<script setup>

import {computed, reactive, ref} from "vue";
import { Lock,Message,ChatLineSquare } from "@element-plus/icons-vue";
import {get,post} from "@/net/index.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const active = ref(0)
const coldTime = ref(0)
const formRef = ref()

const form = reactive({
  email:'',
  code:'',
  password:'',
  password_repeat:''
})

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

    get(`api/auth/ask-code?email=${form.email}&type=reset`, () => {
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

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'));
  } else
    callback()
}

const rule = {
  email: [
    {required: true, message: "请输入邮箱地址", trigger: 'blur'},
    {type: 'email', message: "请输入正确的邮箱地址", trigger: ['blur', 'change']}
  ],
  code: [
    {required: true, message: "请输入验证码", trigger: 'blur'}
  ],password: [
    {required: true, message: "请输入密码", trigger: 'blur'},
    {min: 6, max: 20, message: "密码的长度必须在6-20个字符", trigger: ['blur', 'change']}
  ],
  password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change'] }
  ]
}

function confirmReset(){
  formRef.value.validate((valid) => {
    if (valid) {
      post('api/auth/reset-confirm', {
        email: form.email,
        code: form.code
      }, () => active.value++)
    }
  })
}

function doReset() {
  formRef.value.validate((valid) => {
    if (valid) {
      post('api/auth/reset-password', {...form}, () => {
        ElMessage.success("密码重置成功，请重新登录");
        router.push('/');
      })
    }
  })
}

const isEmailVerify = computed(() => /^[\w.-]+@[\w.-]+\.\w+$/.test(form.email))
</script>

<template>
  <div style="text-align: center">
    <div style="margin-top: 50px">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="验证电子邮件"/>
        <el-step title="重新设定密码"/>
      </el-steps>
    </div>
    <div style="margin: 0 20px" v-if="active === 0">
      <div style="margin-top: 80px">
        <div style="font-size: 25px;font-weight: bold">重置密码</div>
        <div style="font-size: 14px;color: gray">请输入需要重置密码的电子邮件</div>
      </div>
      <div style="margin-top: 50px;">
        <el-form :model="form" :rules="rule" ref="formRef">
          <el-form-item prop="email">
            <el-input v-model="form.email" type="email" placeholder="电子邮件地址">
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
        <el-button @click="confirmReset" style="width: 270px" type="warning" plain>开始重置密码</el-button>
      </div>
    </div>
    <div style="margin: 0 20px" v-if="active === 1">
      <div style="margin-top: 80px">
        <div style="font-size: 25px;font-weight: bold">重置密码</div>
        <div style="font-size: 14px;color: gray">请输入需要重置的密码</div>
      </div>
      <div style="margin-top: 50px">
        <el-form :model="form" :rules="rule" ref="formRef">
          <el-form-item prop="password">
            <el-input v-model="form.password" maxlength="20" type="password" placeholder="密码">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password_repeat">
            <el-input v-model="form.password_repeat" maxlength="20" type="password" placeholder="再次确认密码">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
        <div style="margin-top: 50px;">
          <el-button @click="doReset" style="width: 270px" type="danger" plain>立即重置密码</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>