# Dựng lại video demo

Cần: Python 3.10+, Node 22+, ffmpeg, Microsoft Edge; hệ thống local chạy ở `http://localhost:8080`.

1. Chạy kịch bản để có response thật: `python demo/run_demo.py --env local` (và `--env cloud --base-url https://d3jaw9bd80q6ki.cloudfront.net`).
2. Chụp giao diện: `node ui_capture.mjs <scenario.json> <thư mục>` (scenario sinh bởi `make_scenarios.py`); ảnh giao diện trên cloud dựng bằng `cloud-snap/build_snaps.py`.
3. Dựng khung hình và file phụ đề: `python build_video2.py <repo> <workdir> render` → `png2/`, `timeline2.json`, `demo-ngoc-huyen.srt` (thuật ngữ được phiên âm bởi `tts_text.py`).
4. Tạo giọng: Vbee Studio → **Chuyển phụ đề** → tải `demo-ngoc-huyen.srt`, giọng **HN - Ngọc Huyền**, mp3 (≈ 1 điểm/ký tự; Vbee tự căn lời nói theo mốc của từng câu).
5. Ghép: `python encode2.py <workdir> <file.mp3> demo/video/MiddleJavaStore_Demo_NgocHuyen.mp4`.
