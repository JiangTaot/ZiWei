// ZiWei DouShu constants and reference data
// Contains Heavenly Stems, Earthly Branches, stars, palaces, and utility mappings

// === Heavenly Stems (天干) ===
export const HEAVENLY_STEMS = ['甲', '乙', '丙', '丁', '戊', '己', '庚', '辛', '壬', '癸']

// === Earthly Branches (地支) ===
export const EARTHLY_BRANCHES = ['子', '丑', '寅', '卯', '辰', '巳', '午', '未', '申', '酉', '戌', '亥']

// === Five Elements (五行) ===
export const FIVE_ELEMENTS = ['木', '火', '土', '金', '水']

// === Element mapping for Heavenly Stems ===
export const STEM_ELEMENT_MAP = {
  '甲': '木', '乙': '木',
  '丙': '火', '丁': '火',
  '戊': '土', '己': '土',
  '庚': '金', '辛': '金',
  '壬': '水', '癸': '水',
}

// === Element mapping for Earthly Branches ===
export const BRANCH_ELEMENT_MAP = {
  '寅': '木', '卯': '木',
  '巳': '火', '午': '火',
  '辰': '土', '戌': '土', '丑': '土', '未': '土',
  '申': '金', '酉': '金',
  '亥': '水', '子': '水',
}

// === Yin-Yang mapping for stems ===
export const STEM_YIN_YANG = {
  '甲': '阳', '丙': '阳', '戊': '阳', '庚': '阳', '壬': '阳',
  '乙': '阴', '丁': '阴', '己': '阴', '辛': '阴', '癸': '阴',
}

// === Yin-Yang mapping for branches ===
export const BRANCH_YIN_YANG = {
  '子': '阳', '寅': '阳', '辰': '阳', '午': '阳', '申': '阳', '戌': '阳',
  '丑': '阴', '卯': '阴', '巳': '阴', '未': '阴', '酉': '阴', '亥': '阴',
}

// === Sexagenary cycle (六十甲子) - Combined Stem-Branch pairs ===
export const SEXAGENARY_CYCLE = (() => {
  const cycle = []
  for (let i = 0; i < 60; i++) {
    cycle.push(HEAVENLY_STEMS[i % 10] + EARTHLY_BRANCHES[i % 12])
  }
  return cycle
})()

// === 12 Time Periods (时辰) ===
export const TIME_PERIODS = [
  { index: 0, name: '子时', range: '23:00-01:00', branch: '子' },
  { index: 1, name: '丑时', range: '01:00-03:00', branch: '丑' },
  { index: 2, name: '寅时', range: '03:00-05:00', branch: '寅' },
  { index: 3, name: '卯时', range: '05:00-07:00', branch: '卯' },
  { index: 4, name: '辰时', range: '07:00-09:00', branch: '辰' },
  { index: 5, name: '巳时', range: '09:00-11:00', branch: '巳' },
  { index: 6, name: '午时', range: '11:00-13:00', branch: '午' },
  { index: 7, name: '未时', range: '13:00-15:00', branch: '未' },
  { index: 8, name: '申时', range: '15:00-17:00', branch: '申' },
  { index: 9, name: '酉时', range: '17:00-19:00', branch: '酉' },
  { index: 10, name: '戌时', range: '19:00-21:00', branch: '戌' },
  { index: 11, name: '亥时', range: '21:00-23:00', branch: '亥' },
]

// === 12 Palaces (十二宫) ===
export const TWELVE_PALACES = [
  { index: 0, name: '命宫', meaning: '代表本人性格、天赋、运势之根本' },
  { index: 1, name: '兄弟宫', meaning: '代表兄弟姐妹、朋友同僚关系' },
  { index: 2, name: '夫妻宫', meaning: '代表婚姻、配偶、感情生活' },
  { index: 3, name: '子女宫', meaning: '代表子女、晚辈、生育运' },
  { index: 4, name: '财帛宫', meaning: '代表财富、收入、理财能力' },
  { index: 5, name: '疾厄宫', meaning: '代表健康、疾病、灾厄' },
  { index: 6, name: '迁移宫', meaning: '代表外出、变动、远行运势' },
  { index: 7, name: '交友宫', meaning: '代表朋友、社交、下属关系' },
  { index: 8, name: '官禄宫', meaning: '代表事业、功名、职业发展' },
  { index: 9, name: '田宅宫', meaning: '代表房产、家庭、居住环境' },
  { index: 10, name: '福德宫', meaning: '代表福气、精神享受、晚年运' },
  { index: 11, name: '父母宫', meaning: '代表父母、长辈、师长缘分' },
]

// === Major Stars (十四主星) ===
export const MAJOR_STARS = [
  { name: '紫微', element: '土', type: '帝星', yinYang: '阴' },
  { name: '天机', element: '木', type: '善星', yinYang: '阴' },
  { name: '太阳', element: '火', type: '中星', yinYang: '阳' },
  { name: '武曲', element: '金', type: '财星', yinYang: '阴' },
  { name: '天同', element: '水', type: '福星', yinYang: '阳' },
  { name: '廉贞', element: '火', type: '囚星', yinYang: '阴' },
  { name: '天府', element: '土', type: '库星', yinYang: '阳' },
  { name: '太阴', element: '水', type: '富星', yinYang: '阴' },
  { name: '贪狼', element: '木', type: '桃花', yinYang: '阳' },
  { name: '巨门', element: '水', type: '暗星', yinYang: '阴' },
  { name: '天相', element: '水', type: '印星', yinYang: '阳' },
  { name: '天梁', element: '土', type: '寿星', yinYang: '阳' },
  { name: '七杀', element: '金', type: '将星', yinYang: '阴' },
  { name: '破军', element: '水', type: '耗星', yinYang: '阴' },
]

// === Auxiliary Stars (辅星/杂曜) ===
export const AUXILIARY_STARS = [
  // 六吉星 (Six Auspicious Stars)
  '文昌', '文曲', '左辅', '右弼', '天魁', '天钺',
  // 六煞星 (Six Inauspicious Stars)
  '擎羊', '陀罗', '火星', '铃星', '地空', '地劫',
  // 其他辅星 (Other Auxiliary Stars)
  '禄存', '天马', '天刑', '天姚', '天哭', '天虚',
  '红鸾', '天喜', '龙池', '凤阁', '三台', '八座',
  '恩光', '天贵', '天才', '天寿', '解神', '阴煞',
]

// === Brightness Levels (亮度等级) ===
export const BRIGHTNESS_LEVELS = ['庙', '旺', '得', '利', '平', '不', '陷']

// === Four Transformations (四化) ===
export const FOUR_TRANSFORMATIONS = ['化禄', '化权', '化科', '化忌']

// === Lunar Mansions (二十八宿) ===
export const LUNAR_MANSIONS = [
  '角', '亢', '氐', '房', '心', '尾', '箕',  // East - 青龙
  '斗', '牛', '女', '虚', '危', '室', '壁',  // North - 玄武
  '奎', '娄', '胃', '昴', '毕', '觜', '参',  // West - 白虎
  '井', '鬼', '柳', '星', '张', '翼', '轸',  // South - 朱雀
]

// === Gender Options ===
export const GENDER_OPTIONS = [
  { value: 'male', label: '男' },
  { value: 'female', label: '女' },
]

// === Chart Type (盘类型) ===
export const CHART_TYPES = {
  natal: '本命盘',     // Natal chart
  decade: '大限盘',    // 10-year fortune
  year: '流年盘',      // Yearly fortune
  month: '流月盘',     // Monthly fortune
  day: '流日盘',       // Daily fortune
  hour: '流时盘',      // Hourly fortune
}

/**
 * Get element for a given stem-branch pair
 * @param {string} stemBranch - Combined stem-branch (e.g., '甲子')
 * @returns {string|null} - Five element or null
 */
export function getElement(stemBranch) {
  if (!stemBranch || stemBranch.length < 2) return null
  return STEM_ELEMENT_MAP[stemBranch[0]] || null
}

/**
 * Get time period index by branch name
 * @param {string} branch - Earthly branch name
 * @returns {number} - Time period index (0-11)
 */
export function getTimeIndexByBranch(branch) {
  return EARTHLY_BRANCHES.indexOf(branch)
}

/**
 * Get palace name by index
 * @param {number} index - Palace index (0-11)
 * @returns {string} - Palace name
 */
export function getPalaceName(index) {
  return TWELVE_PALACES[index]?.name || '未知'
}

/**
 * Get palace meaning by index
 * @param {number} index - Palace index (0-11)
 * @returns {string} - Palace meaning
 */
export function getPalaceMeaning(index) {
  return TWELVE_PALACES[index]?.meaning || ''
}
