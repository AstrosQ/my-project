import axios from "axios";
import {ElMessage} from "element-plus";

const authItemName = "access_token"
const defaultFailure = (message, code, url) =>{
    console.warn(`请求地址: ${url},状态码: ${code},错误信息: ${message}`)
    ElMessage.warning('用户名或密码错误')  // 弹窗警告
}

// token获取
function takeAccessToken(){
    // 从其中一个拿到token，没有就为空
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName)
    // str为空说明用户没有登陆
    if (!str) return null
    // 重新封装成验证实体
    const authObj = JSON.parse(str)
    // 判断是否超过过期时间
    if (authObj.expire <= new Date()) {
        deleteAccessToken()
        ElMessage.warning('登录状态已过期，请重新登录！')
        return null
    }
    return authObj.token
}

// token保存
function storeAccessToken(remember, token, expire) {
    const authObj = {
        token: token,
        expire: expire}  // 封装为验证实体
    const str = JSON.stringify(authObj)
    // 如果勾选了记住我，就存储在本地，不然就存储在当前会话中
    if (remember)
        localStorage.setItem(authItemName, str)
    else
        sessionStorage.setItem(authItemName, str)
}

// token删除
function deleteAccessToken(){
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
}

// 返回请求头
function accessHeader(){
    const token = takeAccessToken();
    return token ? {
        'Authorization': `Bearer ${takeAccessToken()}`
    } : {}
}

const defaultError = (error) => {
    console.error(error);
    ElMessage.error('发生了一些错误，请联系管理员')
}

function internalPost(url, data, header, success, failure, error = defaultError){
    axios.post(url,data,{headers:header}).then(({data}) =>{
        if (data.code === 200){
            success(data.data)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))  // 跨域等请求错误
}

function internalGet(url, header, success, failure, error = defaultError){
    axios.get(url,{headers:header}).then(({data}) =>{
        if (data.code === 200){
            success(data.data)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))  // 跨域等请求错误
}

// 外部使用get,post
function get(url, success, failure = defaultFailure()) {
    internalGet(url, accessHeader(), success, failure)
}

function post(url, data, success, failure = defaultFailure()){
    internalPost(url, data, accessHeader(), success, failure)
}
function login(username, password, remember, success, failure = defaultFailure) {
    internalPost('/api/auth/login',{
        username: username,
        password: password
    },{
        'Content-Type': 'application/x-www-form-urlencoded'
    },(data) =>{
        storeAccessToken(remember, data.token, data.expire)
        ElMessage.success(`登录成功，欢迎 ${data.username} 来到我们的系统`)
        success(data)
    },failure)
}

function logout( success, failure = defaultFailure){
    get('/api/auth/logout', () => {
        deleteAccessToken()
        ElMessage.success('退出登录成功，欢迎您再次使用')
        success()
    }, failure)
}

function unauthorized() {
    return !takeAccessToken()
}

export { login, logout, get, post, unauthorized }