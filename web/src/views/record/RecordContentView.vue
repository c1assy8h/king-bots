<template>
    <PlayGround v-if="$store.state.pk.status === 'playing'" />
</template>

<script>
import PlayGround from '../../components/PlayGround.vue'

export default {
    components: {
        PlayGround,
    },
    setup() {

    }
}
</script>

<style scoped>
</style>