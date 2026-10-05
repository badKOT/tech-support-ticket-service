FROM node:22-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm ci

COPY . .

RUN npm run build

FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=builder /app/dist /usr/share/nginx/html

ARG APP_VERSION=unknown
RUN printf '%s\n' "$APP_VERSION" > /usr/share/nginx/html/version.txt

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]