const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

export class ApiError extends Error {
  constructor(message, options = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = options.status || 0
    this.errorType = options.errorType || 'SYSTEM_ERROR'
    this.rawMessage = options.rawMessage || message
    this.payload = options.payload || null
  }
}

export async function request(path, options = {}) {
  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
    ...(options.headers || {})
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method || 'GET',
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined
  })

  let payload
  try {
    payload = await response.json()
  } catch (error) {
    throw new ApiError(`请求失败：HTTP ${response.status}`, {
      status: response.status,
      errorType: 'HTTP_ERROR'
    })
  }

  if (!response.ok || payload.code !== 0) {
    throw new ApiError(formatErrorMessage(payload, response.status), {
      status: response.status,
      errorType: payload.errorType,
      rawMessage: payload.message,
      payload
    })
  }

  return payload.data
}

function formatErrorMessage(payload, status) {
  const message = payload?.message || `HTTP ${status}`
  const errorType = payload?.errorType || ''
  const llmMessages = {
    LLM_CONFIGURATION: 'LLM 配置不完整：请检查模型接口地址和 API Key。',
    LLM_AUTHENTICATION: 'LLM 认证失败：请检查 API Key 是否正确或是否有权限。',
    LLM_RATE_LIMIT: 'LLM 调用被限流：请稍后重试或检查额度。',
    LLM_MODEL: 'LLM 模型不可用：请检查模型名称是否为当前账号可用的模型 ID。',
    LLM_ENDPOINT: 'LLM 接口地址不可用：请检查 base-url 是否包含 /chat/completions。',
    LLM_TIMEOUT: 'LLM 调用超时：请检查网络或适当调大 timeout。',
    LLM_NETWORK: 'LLM 网络请求失败：请检查代理、网络或模型服务状态。',
    LLM_RESPONSE_FORMAT: 'LLM 响应格式异常：模型返回内容无法被后端解析。',
    LLM_UPSTREAM: 'LLM 服务调用失败：请查看后台 Agent 调用日志中的失败原因。'
  }

  if (llmMessages[errorType]) {
    return `${llmMessages[errorType]}${message ? `（${message}）` : ''}`
  }
  return message
}
