FROM node:22-alpine AS build

WORKDIR /app
COPY package.json ./
COPY index.html ./
COPY scripts scripts
COPY src src
COPY vendor vendor
RUN npm run build

FROM nginx:1.29-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
