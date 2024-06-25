<script setup>
import {computed, reactive, ref} from 'vue';
import { User,Lock,Message,ChatLineSquare } from "@element-plus/icons-vue";
import router from "@/router/index.js";
import {ElMessage} from "element-plus";
import {get, post} from "@/net/index.js";

const coldTime = ref(0)
const formRef = ref()

const form = reactive({
  username: '',
  password: '',
  password_repeat: '',
  email: '',
  code: '',
})

const validateUsername = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入用户名'));
  }else if (!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)) {
    callback(new Error('用户名只能包含中文、字母和数字'));
  }else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'));
  } else
    callback()
}

const rule = {
  username: [
    { validator: validateUsername, trigger: ['blur', 'change']}
  ],
  password: [
    {required: true, message: "请输入密码", trigger: 'blur'},
    {min: 6, max: 20, message: "密码的长度必须在6-20个字符", trigger: ['blur', 'change']}
  ],
  password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change'] }
  ],
  email: [
      {required: true, message: "请输入邮箱地址", trigger: 'blur'},
      {type: 'email', message: "请输入正确的邮箱地址", trigger: ['blur', 'change']}
  ],
  code: [
      {required: true, message: "请输入验证码", trigger: 'blur'}
  ]
}

function askCode() {
  if (isEmailVerify) {
    coldTime.value = 60;

    // 保存 setInterval 返回的计时器 ID
    const timerId = setInterval(() => {
      if (coldTime.value > 0) {
        coldTime.value--;
      } else {
        clearInterval(timerId);  // 停止计时器
      }
    }, 1000);

    get(`api/auth/ask-code?email=${form.email}&type=register`, () => {
      ElMessage.success(`验证码已发送到${form.email},注意查收`);
    }, (message) => {
      ElMessage.warning(message);
      coldTime.value = 0;
      clearInterval(timerId);  // 在错误回调中也停止计时器
    });
  } else {
    ElMessage.warning("请输入正确的邮箱地址");
  }
}

function register() {
  formRef.value.validate((valid) => {
    if (valid) {
      post('api/auth/register', {...form}, () =>{
        ElMessage.success('注册成功，欢迎加入我！');
        router.push('/');
      })
    }else {
      ElMessage.warning('请检查输入的信息是否正确');
    }
  })
}

const isEmailVerify = computed(() => /^[\w.-]+@[\w.-]+\.\w+$/.test(form.email))
</script>

<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top: 150px">
      <div style="font-size: 25px;font-weight: bold">注册</div>
      <div style="font-size: 14px;color: grey">欢迎注册我们的网站，请在下方填写相关信息</div>
    </div>
    <div style="margin-top: 50px">
      <el-form :model="form" :rules="rule" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" maxlength="10" type="text" placeholder="用户名">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" maxlength="20" type="password" placeholder="密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password_repeat">
          <el-input v-model="form.password_repeat" maxlength="20" type="password" placeholder="确认密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" maxlength="20" type="text" placeholder="邮箱地址">
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code">
          <el-row :gutter="10">
            <el-col :span="18">
                <el-input v-model="form.code" maxlength="6" type="text" placeholder="验证码">
                  <template #prefix>
                    <el-icon><ChatLineSquare /></el-icon>
                  </template>
                </el-input>
            </el-col>
            <el-col :span="6">
              <el-button @click="askCode" :disabled="!isEmailVerify || coldTime" type="success" plain>
                {{coldTime > 0 ? `${coldTime}秒后重发` : '获取验证码'}}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
    </div>
    <div style="margin-top: 50px;">
      <el-button @click="register" style="width: 270px" type="warning" plain>立即注册</el-button>
    </div>
    <div style="margin-top: 30px">
        <span style="font-size: 15px;color: gray">已有账号？</span>
        <el-link @click="router.push('/')" style="transform: translateY(-2px);">立即登录</el-link>
      </div>
  </div>
</template>

<style scoped>

</style>