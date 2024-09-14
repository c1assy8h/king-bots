import { createRouter, createWebHistory } from 'vue-router'
import PkIndex from '../views/pk/PkIndex'
import NotFound from '../views/error/NotFound'
import RanklistIndex from '../views/ranklist/RanklistIndex'
import UserBotIndex from '../views/user/bots/UserBotIndex'
import RecordIndex from '../views/record/RecordIndex'
import RecordContentView from '../views/record/RecordContentView.vue'
import UserAccountLoginView from '../views/user/account/UserAccountLoginView.vue'
import UserAccountRegisterView from '../views/user/account/UserAccountRegisterView.vue'
import store from "../store/index"

const routes = [
  {
    path: "/",
    name: "home",
    redirect: "/pk/",
    meta: {
      requestAuth: true, //是否要授权
    }
  },
  {
    path: "/pk/",
    name: "pk_index",
    component: PkIndex,
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/ranklist/",
    name: "ranklist_index",
    component: RanklistIndex,
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/user/bots/",
    name: "user_bot_index",
    component: UserBotIndex,
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/user/account/login/",
    name: "user_account_login",
    component: UserAccountLoginView,
    meta: {
      requestAuth: false,
    }
  },
  {
    path: "/user/account/register/",
    name: "user_account_register",
    component: UserAccountRegisterView,
  },
  {
    path: "/record/",
    name: "record_index",
    component: RecordIndex,
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/record/:recordId/",
    name: "record_content",
    component: RecordContentView,
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/404/",
    name: "404",
    component: NotFound,
  },
  {
    path: "/:catchAll(.*)",
    redirect: "/404/",
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

//每次通过router进入页面前调用，to跳转到(登录)页面，from表示从 要授权页面跳转过去 next下一步操作
router.beforeEach((to, from, next) => {
  if(to.meta.requestAuth && !store.state.user.is_login) {
    next({name: "user_account_login"});
  } else{
    next();
  }
})
export default router
