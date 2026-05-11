"""문서 분석용 마이크로서비스 엔트리포인트. CI/ECR 이미지 기동 검증용 최소 구현."""

from fastapi import FastAPI

app = FastAPI(title="document-analyzer")


@app.get("/health")
def health():
    return {"status": "ok"}
