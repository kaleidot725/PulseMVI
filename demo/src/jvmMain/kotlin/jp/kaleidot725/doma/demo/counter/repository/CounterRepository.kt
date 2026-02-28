package jp.kaleidot725.doma.demo.counter.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CounterRepository {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    fun increment() {
        _count.update { it + 1 }
    }

    fun decrement() {
        _count.update { it - 1 }
    }

    fun reset() {
        _count.update { 0 }
    }
}
