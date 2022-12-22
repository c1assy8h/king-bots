<template>
  <div>Bot name: {{ bot_name }}</div>
  <div>Bot rating: {{ bot_rating }}</div>
  <router-view/>
</template>

<script>
import $ from 'jquery'
import { ref } from 'vue';

export default {
  name: "software",
  setup: () => {
    let bot_name = ref("");
    let bot_rating = ref("");

    //从后端中取出值在前端使用
    $.ajax({
      url: "http://localhost:3000/pk/getbotinfo/",
      type: "get",
      success: resp => (
        bot_name.value = resp.name,
        bot_rating.value = resp.rating
      )
  });
    return {
      bot_name,
      bot_rating
    }
  }
}
</script>

<style>
body {
  background-image: url("./assets/background.png");
  background-size: cover;
}
</style>
