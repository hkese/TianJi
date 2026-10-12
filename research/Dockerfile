FROM python:3.12-slim
WORKDIR /app
COPY . /app
RUN pip install --no-cache-dir . && useradd -m researcher && mkdir -p /app/output && chown -R researcher /app
USER researcher
EXPOSE 8000
CMD ["uvicorn", "tianji_research.api:create_app", "--factory", "--host", "0.0.0.0", "--port", "8000"]
