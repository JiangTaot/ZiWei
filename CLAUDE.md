# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

紫薇斗数 (ZiWei DouShu) WeChat Mini Program — an astrology chart calculation app with AI-powered interpretation. Two-module monorepo: Spring Boot backend + uni-app Vue3 frontend.

## Development Commands

```bash
# Backend (ziwei-server/)
cd ziwei-server
mvn spring-boot:run              # Start server on port 48121
mvn clean compile                # Compile check

# Frontend (ziwei-app/)
cd ziwei-app
npm run dev:mp-weixin            # Dev build with watch (outputs to dist/dev/mp-weixin/)
npm run build:mp-weixin          # Production build → dist/build/mp-weixin/
# Open dist/build/mp-weixin/ in WeChat DevTools to preview
```

There is no test suite yet (only backend engine unit tests exist). H5 dev mode (`npm run dev:h5`) is known-broken with uni-app Vue3 alpha — use WeChat DevTools instead.

## Architecture

### Backend (`ziwei-server/`)
Spring Boot 3.3.5 / JDK 17 / MyBatis-Plus 3.5.10 / MySQL 8.0 / Redis.

The core is the **12-step chart calculation pipeline** in `engine/`:
1. `calendar/` — BaziCalculator + LunarCalendarConverter (solar→lunar, 八字 pillars)
2. `palace/` — MingGong (命宫), ShenGong (身宫), 12 palaces setup, WuXingJu (五行局)
3. `star/` — ZiWei star placement → TianFu star → auxiliary/minor stars → brightness → SiHua (四化)
4. `cycle/` — DaXian (大限 decade cycles) + LiuNian (流年 yearly cycles)
5. `pattern/` — 36 pattern/grid matches (格局)

The engine was ported from a reference implementation (YunDao) and verified against 文墨天机 app output.

- `controller/` — ZiweiChartController (calculate, get, list, delete), ZiweiUserController (WeChat login), ZiweiAiController (AI interpretation, sync + SSE streaming)
- `config/` — JWT interceptor, WebMvc CORS, Spring AI (DeepSeek) config
- `enums/` — 14 enums: Tiangan, Dizhi, Palace, Star, Brightness, Sihua, WuxingJu, FiveElement, YinYang, Gender, ErrorCode, etc.
- `converter/` — ChartConverter: engine models → API DTOs
- `exception/` — BusinessException with domain error codes (`1-041-xxx-xxx`), GlobalExceptionHandler

API base path: `/api/ziwei/`. Auth is optional for chart endpoints, required for AI endpoints (JWT Bearer token).

**Persistence strategy**: Chart results are saved across 5 tables (chart, palace, star_position, sihua, daxian) plus a JSON snapshot in `chart_json`. On retrieval, JSON deserialization is tried first (fast path), falling back to DB table reconstruction. `calculateAndSave` gracefully catches persistence errors — the user always gets results even if MySQL is down.

**Known engine quirks**: Late 子时 (23:00–00:00) births get their day incremented by 1 for star placement. Frontend sends DiZhi index (0–11) → backend multiplies by 2 to get solar hour before engine calculation.

### Frontend (`ziwei-app/`)
uni-app Vue3 (alpha tag) / Vite 5 / Pinia 2 / SCSS.

**V2 Architecture — `src/core/` framework layer** (inspired by yudao-mall-uniapp):
- `core/index.js` — singleton `zw` object: `zw.$api`, `zw.$store()`, `zw.$router`, `zw.$platform`, `zw.$helper`
- `core/api/` — auto-discovery via `import.meta.glob('./*/*.js')`. Each domain (chart, user, ai) is a subdirectory with `index.js`
- `core/store/` — auto-discovery via `import.meta.glob('./*.js')`. Each file exports a Pinia `defineStore`. Access via `$store('name')` — returns the store instance
- `core/request/` — HTTP wrapper around `uni.request` with loading counter, auth token injection, 401 handling, error toast mapping
- `core/router/` — navigation helpers with double-tap throttling, auto switchTab for tabbar pages
- `core/platform/` — WeChat capsule button / navbar height / network detection
- `core/config/` — env-driven config: `ZIWEI_BASE_URL`, `ZIWEI_DEV_BASE_URL`, `ZIWEI_API_PATH`, `ZIWEI_TIMEOUT`, `ZIWEI_AI_TIMEOUT`
- `core/hooks/` — Vue composables: `useChart` (calculate/load/mock chart), `useAuth` (WeChat login flow)

**Pages** (4 total, 3 tabbar + 1 subpackage):
| Page | Path | Description |
|------|------|-------------|
| 首页 | `pages/index/index` | Landing + chart history list |
| 排盘 | `pages/chart/index` | Birth form → calculate |
| 命盘 | `pages/result/index` | Chart display (subpackage, preloaded) |
| 我的 | `pages/mine/index` | Profile / settings |

**Components** use `zw-` prefix with easycom auto-discovery defined in `pages.json`:
`"^zw-(.*)": "@/components/zw-$1/zw-$1.vue"`

- `zw-chart-board` — Canvas-based 4×4 palace grid with vertical text and merged center cell
- `zw-birth-form` — Birth info form (solar date, gender, birthplace)
- `zw-palace-detail` — Single palace detail popup
- `zw-ai-panel` — AI interpretation display panel
- `zw-star-icon` — Individual star icon/name display

**Design System** (`uni.scss`): "深邃墨韵" Dark Ink Elegance theme. Key tokens:
- Primary: cinnabar red `#C04B3E`, Gold: `#B8976E`, Jade: `#5B8C5A`
- Typography: Songti/Serif font stack, `rpx` units
- Z-index: dropdown 100, overlay 200, modal 300, toast 400
- uView Plus theme variables are overridden at the bottom of the file

## Environment Variables

Frontend uses `ZIWEI_` prefix (configured in `vite.config.js` → `envPrefix`). Create `.env` in `ziwei-app/`:

```
ZIWEI_BASE_URL=http://192.168.0.102:48121
ZIWEI_API_PATH=/api/ziwei
```

Backend config is in `ziwei-server/src/main/resources/application-dev.yml` (gitignored). Database at `127.0.0.1:13306` (database name: `ziwei`), Redis at `127.0.0.1:16379`. Database schema DDL is at `ziwei-server/src/main/resources/db/schema.sql` (6 tables: user, chart, palace, star_position, sihua, daxian).

Frontend reference data for 天干/地支/五行/六十甲子/星曜/亮度/四化 is in `src/core/helper/const.js` — this is the canonical constants file used by components.

## Key Conventions

- **Language**: Comments/docs in Chinese, code/identifiers in English (see memory)
- **Frontend imports**: Use `@/core` alias for the framework layer. Components are auto-discovered, no manual imports needed
- **API calls**: Always via `zw.$api` or the `$api` import from `@/core/api` — never call `uni.request` directly
- **Store access**: `$store('user')` returns the Pinia store instance. Stores auto-persist via `pinia-plugin-persist-uni`
- **SSR app**: `main.js` exports `createApp()` using `createSSRApp` (uni-app requirement)
- **Subpackage**: `pages/result/` is a WeChat subpackage, preloaded from index and chart pages
- **Mock data**: `useChart.getMockChartData()` provides a complete sample chart for offline/preview — always keep this in sync with the real API shape
