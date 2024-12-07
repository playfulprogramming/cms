FROM node:20-alpine AS base
ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"

# Prepare pnpm@9.15.0 (this should match the version in package.json)
RUN corepack enable pnpm
RUN corepack prepare pnpm@9.15.0 --activate

WORKDIR /app

# --------- Install prod dependencies only ---------
FROM base AS prod-deps
COPY package.json pnpm-lock.yaml .
RUN --mount=type=cache,id=pnpm,target=/pnpm/store pnpm install --prod --frozen-lockfile

# -------- Install dev dependencies & build --------
FROM base AS build
COPY . .
RUN --mount=type=cache,id=pnpm,target=/pnpm/store pnpm install --frozen-lockfile
RUN pnpm run build

# ------------ Copy build output & start -----------
FROM base
COPY --from=prod-deps /app/node_modules /app/node_modules
COPY --from=build /app/dist /app/dist
COPY --from=build /app/package.json /app/package.json
ENV NODE_ENV=production
CMD [ "pnpm", "start" ]
