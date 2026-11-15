// Drive headless Edge through the Chrome DevTools Protocol and capture UI screenshots
// plus the bounding boxes of elements to highlight.
// usage: node ui_capture.mjs <scenario.json> <outDir>
import { spawn } from 'node:child_process';
import { mkdirSync, readFileSync, writeFileSync, rmSync } from 'node:fs';
import { join } from 'node:path';
import { tmpdir } from 'node:os';

const [scenarioFile, outDir] = process.argv.slice(2);
const scenario = JSON.parse(readFileSync(scenarioFile, 'utf8'));
mkdirSync(outDir, { recursive: true });
const sleep = (ms) => new Promise(r => setTimeout(r, ms));

const port = 9333 + Math.floor(Math.random() * 500);
const profile = join(tmpdir(), 'mj-edge-' + port);
const edge = spawn('C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', [
  '--headless=new', `--remote-debugging-port=${port}`, `--user-data-dir=${profile}`,
  '--no-first-run', '--hide-scrollbars', '--mute-audio', '--window-size=1600,900', 'about:blank'], { stdio: 'ignore' });

let targets;
for (let i = 0; i < 50; i++) {
  try { targets = await (await fetch(`http://127.0.0.1:${port}/json/list`)).json(); if (targets.length) break; } catch { }
  await sleep(200);
}
const page = targets.find(t => t.type === 'page');
const ws = new WebSocket(page.webSocketDebuggerUrl);
await new Promise(r => ws.addEventListener('open', r, { once: true }));
let seq = 0;
const pending = new Map();
ws.addEventListener('message', (event) => {
  const msg = JSON.parse(event.data);
  if (msg.id && pending.has(msg.id)) { pending.get(msg.id)(msg); pending.delete(msg.id); }
});
const send = (method, params = {}) => new Promise((resolve, reject) => {
  const id = ++seq;
  pending.set(id, (msg) => msg.error ? reject(new Error(method + ': ' + msg.error.message)) : resolve(msg.result));
  ws.send(JSON.stringify({ id, method, params }));
});
const evaluate = async (expression) => {
  const result = await send('Runtime.evaluate', { expression, awaitPromise: true, returnByValue: true });
  if (result.exceptionDetails) throw new Error('JS error: ' + (result.exceptionDetails.exception?.description || result.exceptionDetails.text));
  return result.result.value;
};

const { width = 1600, height = 900 } = scenario.viewport || {};
await send('Page.enable');
await send('Runtime.enable');
await send('Emulation.setDeviceMetricsOverride', { width, height, deviceScaleFactor: 1, mobile: false });
await send('Emulation.setEmulatedMedia', { features: [{ name: 'prefers-color-scheme', value: 'light' }] });

const manifest = [];
try {
  for (const step of scenario.steps) {
    if (step.goto) {
      await send('Page.navigate', { url: scenario.baseUrl + step.goto });
      await sleep(step.wait ?? 1500);
    }
    if (step.js) {
      await evaluate(`(async () => { ${step.js} })()`);
      await sleep(step.wait ?? 1200);
    }
    if (step.files) {
      // Set a file on an <input type=file> (fires input/change like a real file pick)
      const { root } = await send('DOM.getDocument', { depth: -1 });
      const { nodeId } = await send('DOM.querySelector', { nodeId: root.nodeId, selector: step.files.selector });
      await send('DOM.setFileInputFiles', { nodeId, files: [step.files.path] });
      await sleep(step.files.wait ?? 3000);
    }
    if (step.check) {
      console.log('  check', step.check, JSON.stringify(await evaluate(step.check)));
    }
    if (step.shot) {
      const boxes = await evaluate(`(${JSON.stringify(step.boxes || [])}).map(sel => {
        let el;
        if (sel.startsWith('text:')) {
          const wanted = sel.slice(5);
          const candidates = [...document.querySelectorAll('a, span, div, h1, h2, h3, li, td, button, strong')]
            .filter(e => e.textContent.trim() === wanted)
            .map(e => ({ e, r: e.getBoundingClientRect() }))
            .filter(c => c.r.width > 0 && c.r.height > 0 && c.r.top < innerHeight)
            .sort((a, b) => a.r.width * a.r.height - b.r.width * b.r.height);
          el = candidates[0]?.e;
        } else {
          el = document.querySelector(sel);
        }
        if (!el || Number(getComputedStyle(el).opacity) < 0.5) return { sel, missing: true };
        const r = el.getBoundingClientRect();
        return { sel, x: r.x, y: r.y, w: r.width, h: r.height };
      })`);
      const missing = boxes.filter(b => b.missing).map(b => b.sel);
      if (missing.length) console.warn(`  ! ${step.shot}: missing ${missing.join(', ')}`);
      const shot = await send('Page.captureScreenshot', { format: 'png' });
      writeFileSync(join(outDir, step.shot + '.png'), Buffer.from(shot.data, 'base64'));
      manifest.push({ shot: step.shot, boxes: boxes.filter(b => !b.missing), width, height });
      console.log('  shot', step.shot, boxes.length - missing.length, 'boxes');
    }
  }
} finally {
  writeFileSync(join(outDir, 'manifest.json'), JSON.stringify(manifest, null, 2));
  ws.close();
  edge.kill();
  await sleep(500);
  try { rmSync(profile, { recursive: true, force: true }); } catch { }
}
