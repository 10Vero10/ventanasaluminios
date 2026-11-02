# Etapa 1: compilación
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# Etapa 2: nginx sirve el sitio y reenvía /api y /media al backend
FROM nginx:1.29-alpine
# La plantilla se completa al arrancar con estas variables (en Render se reemplazan desde render.yaml).
ENV BACKEND_ORIGIN=http://backend:8080 \
    PROXY_SECRET=""
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
