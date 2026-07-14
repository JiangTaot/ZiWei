<template>
  <view class="chart-board-container">
    <canvas v-show="!hideCanvas" canvas-id="chartBoard" id="chartBoard" class="board-canvas"
      :style="canvasStyle" @tap="onCanvasTap" disable-scroll></canvas>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, getCurrentInstance } from 'vue'
import { EARTHLY_BRANCHES as DZ } from '@/core/helper/const'

const instance = getCurrentInstance()

// ========== Grid Layout (4×4, center 2×2 merged) ==========
const GRID = {
  5:[0,0], 6:[0,1], 7:[0,2], 8:[0,3],
  4:[1,0],                            9:[1,3],
  3:[2,0],                            10:[2,3],
  2:[3,0], 1:[3,1], 0:[3,2], 11:[3,3],
}
const CX = 1, CY = 1, CSPAN = 2

// ========== Ivory-theme color palette ==========
const DK = {
  BG:        '#FFFFFF',
  CELL_BG:   '#FDF9F2',
  BORDER:    '#E8E0D5',
  OUTER:     '#B8976E',
  DZ:        '#C04B3E',
  TG:        '#B8976E',
  STAR_DEF:  '#3A3A3A',
  NAME_DEF:  '#6B6B6B',
  NAME_MG:   '#C04B3E',
  SHEN:      '#5577AA',
  EMPTY:     '#CCC',
  DX_LABEL:  '#8A8780',
  CTX_TEXT:  '#5A5A5A',
  CTX_HINT:  '#9E9E9E',
  CTX_WX:    '#B8976E',
  CTX_BZ:    '#C04B3E',
  MG_BG:     'rgba(192,75,62,0.06)',
  // 三方四正 highlights
  HL_SELF:   'rgba(192,75,62,0.14)',
  HL_TRINE:  'rgba(184,150,110,0.10)',
  HL_OPPO:   'rgba(85,119,170,0.08)',
}
// Star brightness colors
const BC = {
  '庙':'#C03B3B','旺':'#E87830','得':'#4A8C4A','利':'#4A8C4A',
  '平':'#888','不':'#5577AA','陷':'#5577AA',
}
// 四化 colors
const SC = { '禄':'#4ADE80','权':'#60A5FA','科':'#FACC15','忌':'#F87171' }

const P = 6, C4 = 4

// ========== Props ==========
const props = defineProps({
  chartData: { type: Object, default: () => ({}) },
  activeTab: { type: String, default: 'natal' },
  hideCanvas: { type: Boolean, default: false },
})
const emit = defineEmits(['palaceClick'])

// ========== State ==========
const W = ref(375), H = ref(375), CS = ref(0)
const selectedIdx = ref(-1) // DZ index of selected palace, -1 = none
const canvasStyle = computed(() => ({ width: W.value + 'px', height: H.value + 'px' }))

onMounted(() => {
  const sw = uni.getSystemInfoSync().windowWidth
  const cellW = Math.floor((sw - P * 2) / C4)
  const cellH = Math.floor(cellW * 1.35)
  W.value = sw
  H.value = P * 2 + cellH * C4
  nextTick(() => setTimeout(draw, 300))
})
watch(() => [props.chartData, props.activeTab], () => nextTick(() => setTimeout(draw, 100)), { deep: true })
// hideCanvas 使用 v-show（hidden 属性），Canvas 不销毁，无需重绘

// ========== Helpers ==========
function resetText(ctx, a, b) { ctx.setTextAlign(a); ctx.setTextBaseline(b) }

/** Compute 三方四正 set from a given DZ index */
function getSanFang(idx) {
  if (idx < 0) return new Set()
  return new Set([
    idx,                          // 本宫
    (idx + 4) % 12,               // 三合1
    (idx - 4 + 12) % 12,          // 三合2
    (idx + 6) % 12,               // 对宫
  ])
}

// ========== Main Drawing ==========
function draw() {
  const ctx = uni.createCanvasContext('chartBoard', instance?.proxy)
  if (!ctx) return

  const canvasW = W.value, canvasH = H.value
  const cellW = Math.floor((canvasW - P * 2) / C4)
  const cellH = Math.floor(cellW * 1.35)
  CS.value = cellW

  // Font sizes
  const FS_STAR   = Math.round(cellW * 0.13)
  const FS_SIHUA  = Math.round(cellW * 0.10)
  const FS_BRIGHT = Math.round(cellW * 0.08)  // brightness level text
  const FS_NAME   = Math.round(cellW * 0.14)
  const FS_DX     = Math.round(cellW * 0.12)
  const FS_DZ     = Math.round(cellW * 0.14)
  const FS_TG     = Math.round(cellW * 0.11)
  const FS_MARK   = Math.round(cellW * 0.12)
  const STAR_COL_W = FS_STAR + 2
  const STAR_COL_GAP = 1

  const palaces = props.chartData?.palaces || []
  const mg = props.chartData?.mingGong || ''
  const selSet = getSanFang(selectedIdx.value)
  // ── 1. Background ──
  ctx.setFillStyle(DK.BG)
  ctx.fillRect(0, 0, canvasW, canvasH)

  // ── Outer border (gold) ──
  ctx.setStrokeStyle(DK.OUTER)
  ctx.setLineWidth(2)
  ctx.strokeRect(P, P, cellW * C4, cellH * C4)

  // ── 2. Palace cells ──
  palaces.forEach((p) => {
    const dz = p?.dizhi || ''
    const idx = DZ.indexOf(dz)
    if (idx < 0) return
    const pos = GRID[idx]
    if (!pos) return
    const [r, c] = pos
    const x = P + c * cellW, y = P + r * cellH
    const isMg = mg.startsWith(dz)
    const isSg = !!p.isShenGong

    // ── Cell bg: 三方四正 > 命宫 > default ──
    if (selSet.has(idx)) {
      const isSelf = idx === selectedIdx.value
      const isOpp  = idx === (selectedIdx.value + 6) % 12
      ctx.setFillStyle(isSelf ? DK.HL_SELF : isOpp ? DK.HL_OPPO : DK.HL_TRINE)
      ctx.fillRect(x + 1, y + 1, cellW - 2, cellH - 2)
    } else if (isMg) {
      ctx.setFillStyle(DK.MG_BG)
      ctx.fillRect(x + 1, y + 1, cellW - 2, cellH - 2)
    }

    // Cell border
    ctx.setStrokeStyle(DK.BORDER)
    ctx.setLineWidth(0.5)
    ctx.strokeRect(x, y, cellW, cellH)

    // ── 身宫 (top-left) + 大限 (top-right) ──
    if (isSg) {
      resetText(ctx, 'left', 'top')
      ctx.setFontSize(FS_MARK)
      ctx.setFillStyle(DK.SHEN)
      ctx.fillText('身', x + 3, y + 2)
    }
    const dxLabel = p.daXianLabel || ''
    if (dxLabel) {
      resetText(ctx, 'right', 'top')
      ctx.setFontSize(Math.round(FS_MARK * 0.9))
      ctx.setFillStyle(DK.OUTER)
      ctx.fillText(dxLabel, x + cellW - 3, y + 2)
    }

    // ── Stars area ──
    const starAreaTop = y + 6 + ((isSg || dxLabel) ? FS_MARK + 4 : 0)

    // ── Stars ──
    const stars = [
      ...(p.majorStars || []),
      ...(p.auxiliaryStars || []),
      ...(p.minorStars || []),
    ]
    const starAreaL = x + 3
    const starAreaR = x + cellW - 3
    const starAreaBot = y + cellH * 0.75

    if (stars.length > 0) {
      let colX = starAreaL, colY = starAreaTop
      stars.forEach((s) => {
        const nm = typeof s === 'string' ? s : (s.starName || s.name || '')
        if (!nm) return
        const b = typeof s === 'object' ? s.brightness : ''
        const sh = typeof s === 'object' ? (s.sihuaType || s.sihua || '') : ''
        const color = BC[b] || DK.STAR_DEF
        const nameH = nm.length * FS_STAR
        const brightH = b ? FS_BRIGHT + 1 : 0
        const sihuaH = sh ? FS_SIHUA + 1 : 0
        const colH = nameH + brightH + sihuaH

        if (colX + STAR_COL_W > starAreaR) {
          colX = starAreaL
          colY += FS_STAR * 4 + 8  // extra spacing to avoid 四化 overlap
        }
        if (colY + colH > starAreaBot) return

        // Star name (vertical)
        resetText(ctx, 'left', 'top')
        ctx.setFontSize(FS_STAR)
        ctx.setFillStyle(color)
        for (let i = 0; i < nm.length; i++) {
          ctx.fillText(nm[i], colX, colY + i * FS_STAR)
        }
        // Brightness level
        if (b) {
          ctx.setFontSize(FS_BRIGHT)
          ctx.setFillStyle(BC[b] || DK.DX_LABEL)
          ctx.fillText(b, colX + 1, colY + nameH + 1)
        }
        // 四化
        if (sh && SC[sh]) {
          ctx.setFontSize(FS_SIHUA)
          ctx.setFillStyle(SC[sh])
          ctx.fillText(sh, colX + 1, colY + nameH + brightH + 1)
        }
        colX += STAR_COL_W + STAR_COL_GAP
      })
    } else {
      resetText(ctx, 'center', 'middle')
      ctx.setFontSize(FS_STAR)
      ctx.setFillStyle(DK.EMPTY)
      ctx.fillText('空', x + cellW / 2, y + cellH * 0.42)
    }

    // ── Bottom-right: 天干 above 地支 ──
    const tg = p.tianGan || ''
    const gzBotY = y + cellH - 4
    if (tg) {
      resetText(ctx, 'right', 'bottom')
      ctx.setFontSize(FS_TG)
      ctx.setFillStyle(DK.TG)
      ctx.fillText(tg, x + cellW - 4, gzBotY - FS_DZ - 1)
      resetText(ctx, 'right', 'bottom')
      ctx.setFontSize(FS_DZ)
      ctx.setFillStyle(DK.DZ)
      ctx.fillText(dz, x + cellW - 4, gzBotY)
    } else {
      resetText(ctx, 'right', 'bottom')
      ctx.setFontSize(FS_DZ)
      ctx.setFillStyle(DK.DZ)
      ctx.fillText(dz, x + cellW - 4, gzBotY)
    }

    // ── 宫名 (bottom-center) ──
    const name = (p.palaceName || '').replace(/\s+/g, '')
    if (name) {
      resetText(ctx, 'center', 'bottom')
      ctx.setFontSize(FS_NAME)
      ctx.setFillStyle(isMg ? DK.NAME_MG : DK.NAME_DEF)
      ctx.fillText(name, x + cellW / 2, gzBotY)
    }
  })

  // ── 3. Center Area (smaller inner box) ──
  const cmx = P + CX * cellW, cmy = P + CY * cellH
  const cmw = cellW * CSPAN, cmh = cellH * CSPAN
  const inset = Math.round(cellW * 0.35)  // shrink the inner box significantly
  const cbx = cmx + inset, cby = cmy + inset
  const cbw = cmw - inset * 2, cbh = cmh - inset * 2
  const midX = cmx + cmw / 2

  // Clear center bg (no border, no extra fill)
  ctx.setFillStyle(DK.BG)
  ctx.fillRect(cmx + 1, cmy + 1, cmw - 2, cmh - 2)

  // All center text — unified font size
  const ctxFS = Math.round(cellW * 0.13)

  // Helper: hour → 时辰
  function getShichen(hour) {
    const idx = Math.floor(((hour ?? 0) + 1) / 2) % 12
    return DZ[idx] + '时'
  }

  // 公历
  const sy = props.chartData?.solarYear
  if (sy) {
    const sm = String(props.chartData.solarMonth || 1).padStart(2, '0')
    const sd = String(props.chartData.solarDay || 1).padStart(2, '0')
    const sh = String(props.chartData.solarHour ?? 0).padStart(2, '0')
    resetText(ctx, 'center', 'middle')
    ctx.setFontSize(ctxFS)
    ctx.setFillStyle(DK.CTX_TEXT)
    ctx.fillText(`公历 ${sy}-${sm}-${sd} ${sh}:00`, midX, cby + cbh * 0.12)
  }
  // 农历 (年月日时辰)
  const ly = props.chartData?.lunarYear
  if (ly) {
    const lm = props.chartData.lunarMonth || 1
    const ld = props.chartData.lunarDay || 1
    const isLeap = props.chartData.isLeapMonth ? '闰' : ''
    const sc = getShichen(props.chartData.solarHour)
    resetText(ctx, 'center', 'middle')
    ctx.setFontSize(ctxFS)
    ctx.setFillStyle(DK.CTX_HINT)
    ctx.fillText(`农历 ${isLeap}${ly}年${lm}月${ld}日 ${sc}`, midX, cby + cbh * 0.27)
  }
  // 五行局
  const wx = props.chartData?.wuxingJu
  if (wx) {
    resetText(ctx, 'center', 'middle')
    ctx.setFontSize(ctxFS)
    ctx.setFillStyle(DK.CTX_WX)
    ctx.fillText(wx, midX, cby + cbh * 0.42)
  }
  // ★ 当前大限
  const nowYear = new Date().getFullYear()
  const birthYear = props.chartData?.solarYear || 0
  const currentAge = birthYear ? nowYear - birthYear : 0
  const daxians = props.chartData?.daxians || []
  const currentDaxian = daxians.find((d) => currentAge >= d.ageStart && currentAge <= d.ageEnd)
  if (currentDaxian) {
    resetText(ctx, 'center', 'middle')
    ctx.setFontSize(ctxFS)
    ctx.setFillStyle(DK.OUTER)
    ctx.fillText(`大限 ${currentDaxian.ageStart}-${currentDaxian.ageEnd}岁 ${currentDaxian.palaceName}`, midX, cby + cbh * 0.57)
  }
  // 八字
  const bz = [
    props.chartData?.yearPillar, props.chartData?.monthPillar,
    props.chartData?.dayPillar,  props.chartData?.hourPillar,
  ].filter(Boolean)
  if (bz.length === 4) {
    resetText(ctx, 'center', 'middle')
    ctx.setFontSize(ctxFS)
    ctx.setFillStyle(DK.CTX_BZ)
    ctx.fillText(bz.join('  '), midX, cby + cbh * 0.72)
  }
  // 命宫 · 身宫
  const mgInfo = props.chartData?.mingGong || ''
  const sgInfo = props.chartData?.shenGong || ''
  if (mgInfo || sgInfo) {
    const parts = []
    if (mgInfo) parts.push('命宫' + mgInfo.replace('宫', ''))
    if (sgInfo) parts.push('身宫' + sgInfo.replace('宫', ''))
    resetText(ctx, 'center', 'middle')
    ctx.setFontSize(ctxFS)
    ctx.setFillStyle(DK.CTX_BZ)
    ctx.fillText(parts.join('  ·  '), midX, cby + cbh * 0.87)
  }

  // No corner decorations (no border)

  // ── 4. 三方四正: triangle (center + 三合×2) + 四正 radial ──
  if (selectedIdx.value >= 0) {
    const selPos = GRID[selectedIdx.value]
    if (selPos) {
    const cx = P + selPos[1] * cellW + cellW / 2
    const cy = P + selPos[0] * cellH + cellH / 2

    const tri1 = (selectedIdx.value + 4) % 12         // 三合1
    const tri2 = (selectedIdx.value - 4 + 12) % 12    // 三合2
    const oppo = (selectedIdx.value + 6) % 12          // 四正(对宫)

    const pTri1 = GRID[tri1], pTri2 = GRID[tri2], pOppo = GRID[oppo]

    if (pTri1 && pTri2) {
      const t1x = P + pTri1[1] * cellW + cellW / 2
      const t1y = P + pTri1[0] * cellH + cellH / 2
      const t2x = P + pTri2[1] * cellW + cellW / 2
      const t2y = P + pTri2[0] * cellH + cellH / 2

      const dashLen = Math.round(cellW * 0.04)
      ctx.setLineDash([dashLen, dashLen], 0)
      ctx.setLineWidth(1)

      // Triangle: center → 三合1 → 三合2 → center
      ctx.setStrokeStyle('rgba(192,75,62,0.28)')
      ;[[cx,cy,t1x,t1y],[t1x,t1y,t2x,t2y],[t2x,t2y,cx,cy]].forEach(([ax,ay,bx,by]) => {
        ctx.beginPath(); ctx.moveTo(ax, ay); ctx.lineTo(bx, by); ctx.stroke()
      })
    }

    // 四正: center → 对宫 (separate, lighter radial)
    if (pOppo) {
      const ox = P + pOppo[1] * cellW + cellW / 2
      const oy = P + pOppo[0] * cellH + cellH / 2
      const dashLen = Math.round(cellW * 0.04)
      ctx.setLineDash([dashLen, dashLen], 0)
      ctx.setStrokeStyle('rgba(192,75,62,0.18)')
      ctx.beginPath(); ctx.moveTo(cx, cy); ctx.lineTo(ox, oy); ctx.stroke()
    }

    ctx.setLineDash([], 0)
    } // if (selPos)
  }

  ctx.draw()
}

function corner(ctx, x, y, sz, len) {
  ctx.setStrokeStyle(DK.OUTER)
  ctx.setLineWidth(1)
  const g = 2
  const l = (ax, ay, bx, by) => { ctx.beginPath(); ctx.moveTo(ax, ay); ctx.lineTo(bx, by); ctx.stroke() }
  l(x + g, y, x + g + len, y); l(x, y + g, x, y + g + len)
  l(x + sz - g, y, x + sz - g - len, y); l(x + sz, y + g, x + sz, y + g + len)
  l(x + g, y + sz, x + g + len, y + sz); l(x, y + sz - g, x, y + sz - g - len)
  l(x + sz - g, y + sz, x + sz - g - len, y + sz); l(x + sz, y + sz - g, x + sz, y + sz - g - len)
}

// ========== Tap handler (三方四正) ==========
function onCanvasTap(e) {
  const cw = CS.value
  if (!cw || !e.detail) { selectedIdx.value = -1; draw(); return }
  const cellWt = cw
  const cellHt = Math.floor(cw * 1.35)
  const tx = e.detail.x, ty = e.detail.y
  if (tx < 0 || ty < 0) { selectedIdx.value = -1; draw(); return }
  const col = Math.floor(tx / cellWt), row = Math.floor(ty / cellHt)

  // Tap on center area → deselect
  if (row >= CY && row < CY + CSPAN && col >= CX && col < CX + CSPAN) {
    selectedIdx.value = -1
    draw()
    return
  }
  // Find clicked palace
  for (const [k, pos] of Object.entries(GRID)) {
    if (pos[0] === row && pos[1] === col) {
      const idx = +k
      const p = (props.chartData?.palaces || []).find((x) => x.dizhi === DZ[idx])
      if (p) {
        // Toggle: tap same cell again → deselect
        if (selectedIdx.value === idx) {
          selectedIdx.value = -1
        } else {
          selectedIdx.value = idx
        }
        emit('palaceClick', p)
        draw()
      }
      return
    }
  }
  // Tap outside grid → deselect
  selectedIdx.value = -1
  draw()
}

defineExpose({ redraw: draw })
</script>

<style lang="scss" scoped>
.chart-board-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16rpx 0;
}
.board-canvas {
  border-radius: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}
</style>
