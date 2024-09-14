export default {
    state: {
        is_record: false, //默认不是录像
        a_step: "", //记录A的操作
        b_step: "",
    },
    getters: {

    },
    mutations: {
        updateIsRecord(state, is_record) {
            state.is_record = is_record;
        },
        updateSteps(state, data){
            state.a_step = a_step;
            state.b_step = b_step;
        }
    },
    actions: {

    },
    modules: {

    }
}