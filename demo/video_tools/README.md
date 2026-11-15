# Dựng lại video demo

Cần: Python 3.10+, Node 22+, ffmpeg, Microsoft Edge, hệ thống local đang chạy ở `http://localhost:8080`.

```powershell
python demo/run_demo.py --env local
python demo/run_demo.py --env cloud --base-url https://d3jaw9bd80q6ki.cloudfront.net
mkdir build-video; copy demo\video_tools\* build-video\
cd build-video
python make_scenarios.py .
node ui_capture.mjs scenario-local.json shots-local
node ui_capture.mjs scenario-cloud.json shots-cloud
node ui_capture.mjs scenario-github.json shots-web
python build_video.py .. .
python encode.py . ..\demo\video\MiddleJavaStore_Demo.mp4
```

`build_video.py` đọc code thật trong repo và response thật trong `demo/output/*/steps.json`, sinh khung hình HTML 1920×1080 (khung đỏ không chữ), chụp bằng Edge headless, rồi ghi `timeline.json`. `encode.py` encode từng khung đúng thời lượng nên mốc thời gian trong `demo/SCRIPT_NOI.md` khớp chính xác với video.
