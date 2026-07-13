<template>
  <view class="chart-board-container">
    <canvas canvas-id="chartBoard" id="chartBoard" class="board-canvas"
      :style="canvasStyle" @tap="onCanvasTap" disable-scroll></canvas>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, getCurrentInstance } from 'vue'
const instance = getCurrentInstance()

// ========== Constants ==========
const DZ = ['子','丑','寅','卯','辰','巳','午','未','申','酉','戌','亥']
const GRID = { 5:[0,0],6:[0,1],7:[0,2],8:[0,3], 4:[1,0],9:[1,3], 3:[2,0],10:[2,3], 2:[3,0],1:[3,1],0:[3,2],11:[3,3] }
const CX = 1, CY = 1, CSPAN = 2 // center merged area

const BC = { // brightness colors
  '庙':'#C03B3B','旺':'#E87830','得':'#4A8C4A','利':'#4A8C4A','平':'#888','不':'#5577AA','陷':'#5577AA'
}
const SC = { '禄':'#16A34A','权':'#2563EB','科':'#CA8A04','忌':'#DC2626' }

const P = 8, C4 = 4

// ========== Props ==========
const props = defineProps({
  chartData: { type: Object, default: () => ({}) },
  activeTab: { type: String, default: 'natal' },
})
const emit = defineEmits(['palaceClick'])

// ========== State ==========
const W = ref(375), H = ref(375), CS = ref(0), PD = ref(0)
const style = computed(() => ({ width: W.value+'px', height: H.value+'px' }))

onMounted(() => {
  const info = uni.getSystemInfoSync()
  W.value = info.windowWidth; H.value = info.windowWidth
  nextTick(() => setTimeout(draw, 300))
})
watch(() => props.chartData, () => nextTick(() => setTimeout(draw, 100)), { deep: true })

// ========== Drawing ==========
function draw() {
  const ctx = uni.createCanvasContext('chartBoard', instance?.proxy)
  if (!ctx) return

  const cs = (Math.min(W.value, H.value) - P*2) / C4
  CS.value = cs; PD.value = P

  // ── Background ──
  ctx.setFillStyle('#FDFAF5')
  ctx.fillRect(0, 0, W.value, H.value)

  // ── Outer border ──
  ctx.setStrokeStyle('#8B6B4A')
  ctx.setLineWidth(2)
  ctx.strokeRect(P, P, cs*C4, cs*C4)

  // ── Grid lines ──
  ctx.setStrokeStyle('#D4C8B8')
  ctx.setLineWidth(0.5)
  for (let i = 1; i < C4; i++) {
    ctx.beginPath()
    ctx.moveTo(P + i*cs, P); ctx.lineTo(P + i*cs, P + cs*C4)
    ctx.stroke()
    ctx.beginPath()
    ctx.moveTo(P, P + i*cs); ctx.lineTo(P + cs*C4, P + i*cs)
    ctx.stroke()
  }

  // ── Palaces ──
  const palaces = props.chartData?.palaces || []
  const mg = props.chartData?.mingGong || ''
  palaces.forEach(p => {
    const dz = p?.dizhi || ''
    const idx = DZ.indexOf(dz)
    if (idx < 0) return
    const pos = GRID[idx]; if (!pos) return
    const [r, c] = pos
    const x = P + c*cs, y = P + r*cs
    const isMg = mg.startsWith(dz)

    // Cell bg tint
    if (isMg) { ctx.setFillStyle('rgba(200,60,40,0.05)'); ctx.fillRect(x, y, cs, cs) }

    // 地支 — top-left, bold, colored
    ctx.setFontSize(14)
    ctx.setFillStyle('#C03B3B')
    ctx.setTextAlign('left'); ctx.setTextBaseline('top')
    ctx.fillText(dz, x + 6, y + 4)

    // 天干 — top-right, small, gold
    const tg = p.tianGan || ''
    if (tg) {
      ctx.setFontSize(11)
      ctx.setFillStyle('#B8976E')
      ctx.setTextAlign('right'); ctx.setTextBaseline('top')
      ctx.fillText(tg, x + cs - 5, y + 4)
    }

    // 宮名 — vertical, center-left area
    const name = (p.palaceName || '').replace(/\s+/g, '')
    if (name) {
      ctx.setFontSize(13)
      ctx.setFillStyle(isMg ? '#C03B3B' : '#3A3A3A')
      ctx.setTextAlign('left')
      vtext(ctx, name, x + 6, y + 24, 18)
    }

    // 命宫标记
    if (isMg) {
      ctx.setFontSize(9)
      ctx.setFillStyle('#C03B3B')
      ctx.setTextAlign('left'); ctx.setTextBaseline('top')
      ctx.fillText('命', x + 22, y + 5)
    }

    // 身宫标记
    const isSg = p.isShenGong
    if (isSg) {
      ctx.setFontSize(9)
      ctx.setFillStyle('#5577AA')
      ctx.setTextAlign('right'); ctx.setTextBaseline('top')
      ctx.fillText('身', x + cs - 18, y + 5)
    }

    // ── Stars — bottom area, horizontal ──
    const stars = [...(p.majorStars||[]), ...(p.auxiliaryStars||[]), ...(p.minorStars||[])]
    let sx = x + 4, sy = y + cs - 4
    const maxW = cs - 8
    stars.forEach(s => {
      const nm = typeof s === 'string' ? s : (s.starName||s.name||'')
      if (!nm) return
      const b = typeof s === 'object' ? s.brightness : ''
      const sh = typeof s === 'object' ? (s.sihuaType||s.sihua||'') : ''
      const color = BC[b] || '#3A3A3A'
      ctx.setFontSize(10)
      const tw = nm.length * 10 + (sh ? 10 : 0)
      if (sx + tw > x + maxW) { sx = x + 4; sy -= 13 }

      ctx.setFillStyle(color)
      ctx.setTextAlign('left'); ctx.setTextBaseline('bottom')
      ctx.fillText(nm, sx, sy)

      // 四化小标 — superscript style
      if (sh && SC[sh]) {
        ctx.setFontSize(7)
        ctx.setFillStyle(SC[sh])
        ctx.fillText(sh, sx + nm.length * 10 - 2, sy - 4)
      }
      sx += tw + 3
    })
  })

  // ── Center Merged Area ──
  const cx = P + CX*cs, cy = P + CY*cs, cw = cs*CSPAN
  // Erase inner grid
  ctx.setFillStyle('#FDFAF5')
  ctx.fillRect(cx + 1, cy + 1, cw - 2, cw - 2)
  // Gold border
  ctx.setStrokeStyle('#B8976E')
  ctx.setLineWidth(1.5)
  ctx.strokeRect(cx + 3, cy + 3, cw - 6, cw - 6)
  // Subtle inner bg
  ctx.setFillStyle('rgba(184,150,110,0.04)')
  ctx.fillRect(cx + 4, cy + 4, cw - 8, cw - 8)

  const midX = cx + cw/2, midY = cy + cw/2

  // ☯ Taiji
  ctx.setFontSize(36)
  ctx.setFillStyle('#C03B3B')
  ctx.setTextAlign('center'); ctx.setTextBaseline('middle')
  ctx.fillText('☯', midX, midY - 38)

  // 八字
  const bz = [props.chartData?.yearPillar, props.chartData?.monthPillar, props.chartData?.dayPillar, props.chartData?.hourPillar].filter(Boolean)
  if (bz.length === 4) {
    ctx.setFontSize(12)
    ctx.setFillStyle('#3A3A3A')
    ctx.fillText(bz.join('  '), midX, midY - 10)
  }

  // 五行局
  const wx = props.chartData?.wuxingJu
  if (wx) {
    ctx.setFontSize(11)
    ctx.setFillStyle('#B8976E')
    ctx.fillText(wx, midX, midY + 8)
  }

  // 命宫 身宫
  const mgInfo = props.chartData?.mingGong || '', sgInfo = props.chartData?.shenGong || ''
  if (mgInfo || sgInfo) {
    const parts = []
    if (mgInfo) parts.push('命宫' + mgInfo.replace('宫',''))
    if (sgInfo) parts.push('身宫' + sgInfo.replace('宫',''))
    ctx.setFontSize(10)
    ctx.setFillStyle('#C03B3B')
    ctx.fillText(parts.join('  ·  '), midX, midY + 26)
  }

  // Corner decorations on center box
  corner(ctx, cx+3, cy+3, cw-6, 8)

  ctx.draw()
}

// Draw vertical text (one char per line)
function vtext(ctx, s, x, y, lh) {
  for (let i = 0; i < s.length; i++) ctx.fillText(s[i], x, y + i*lh)
}

// Small corner marks
function corner(ctx, x, y, s, len) {
  ctx.setStrokeStyle('#B8976E')
  ctx.setLineWidth(1)
  const g = 3
  // top-left
  ctx.beginPath(); ctx.moveTo(x+g, y); ctx.lineTo(x+g+len, y); ctx.stroke()
  ctx.beginPath(); ctx.moveTo(x, y+g); ctx.lineTo(x, y+g+len); ctx.stroke()
  // top-right
  ctx.beginPath(); ctx.moveTo(x+s-g, y); ctx.lineTo(x+s-g-len, y); ctx.stroke()
  ctx.beginPath(); ctx.moveTo(x+s, y+g); ctx.lineTo(x+s, y+g+len); ctx.stroke()
  // bottom-left
  ctx.beginPath(); ctx.moveTo(x+g, y+s); ctx.lineTo(x+g+len, y+s); ctx.stroke()
  ctx.beginPath(); ctx.moveTo(x, y+s-g); ctx.lineTo(x, y+s-g-len); ctx.stroke()
  // bottom-right
  ctx.beginPath(); ctx.moveTo(x+s-g, y+s); ctx.lineTo(x+s-g-len, y+s); ctx.stroke()
  ctx.beginPath(); ctx.moveTo(x+s, y+s-g); ctx.lineTo(x+s, y+s-g-len); ctx.stroke()
}

// ========== Tap ==========
function onCanvasTap(e) {
  const cs = CS.value, pad = PD.value
  if (!cs || !e.detail) return
  const tx = e.detail.x - pad, ty = e.detail.y - pad
  if (tx < 0 || ty < 0) return
  const c = Math.floor(tx / cs), r = Math.floor(ty / cs)
  if (r >= CY && r < CY+CSPAN && c >= CX && c < CX+CSPAN) return
  for (const [k, pos] of Object.entries(GRID)) {
    if (pos[0] === r && pos[1] === c) {
      const p = (props.chartData?.palaces || []).find(x => x.dizhi === DZ[+k])
      if (p) emit('palaceClick', p)
      return
    }
  }
}

defineExpose({ redraw: draw })
</script>

<style lang="scss" scoped>
.chart-board-container { display:flex; justify-content:center; align-items:center; padding:16rpx 0; }
.board-canvas { border-radius:12rpx; box-shadow:0 4rpx 20rpx rgba(0,0,0,.06); }
</style>
