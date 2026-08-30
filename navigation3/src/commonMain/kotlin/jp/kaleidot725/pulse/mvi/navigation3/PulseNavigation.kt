package jp.kaleidot725.pulse.mvi.navigation3

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseStore

/**
 * Creates a [PulseStore] that survives configuration changes.
 *
 * The Store is owned by a [ViewModel] scoped to the current [ViewModelStoreOwner], so an Android
 * configuration change (rotation, theme switch, ...) recreates the composition without recreating
 * the Store: state is preserved and [PulseStore.onSetup] is not repeated. [PulseStore.onSetup] runs
 * once when the Store is created, and its scope is cancelled only when the owner is cleared, which
 * is when the screen is gone for good.
 *
 * [key] must be unique within the owner. It defaults to the Store class name, so pass an explicit
 * key when the same Store type is used more than once under a single owner.
 */
@Composable
public inline fun <reified Store : PulseStore<*, *, *, *, *>> rememberPulseStore(
    key: String? = null,
    noinline factory: () -> Store,
): Store {
    val resolvedKey = key ?: (Store::class.qualifiedName ?: Store::class.simpleName ?: "PulseStore")
    val store = rememberPulseStoreHolder(resolvedKey, factory).store
    check(store is Store) {
        "Key \"$resolvedKey\" is already used by ${store::class.simpleName}. Pass a unique key to rememberPulseStore()."
    }
    return store
}

/**
 * Creates a [PulseContainer] that survives configuration changes.
 *
 * The Container is owned by a [ViewModel] scoped to the current [ViewModelStoreOwner], which keeps
 * its Unicast subscriptions alive across recompositions and configuration changes. The Container
 * scope is cancelled through [PulseContainer.close] when the owner is cleared.
 *
 * [key] must be unique within the owner. It defaults to the Container class name.
 */
@Composable
public inline fun <reified Container : PulseContainer<*, *>> rememberPulseContainer(
    key: String? = null,
    noinline factory: () -> Container,
): Container {
    val resolvedKey = key ?: (Container::class.qualifiedName ?: Container::class.simpleName ?: "PulseContainer")
    val container = rememberPulseContainerHolder(resolvedKey, factory).container
    check(container is Container) {
        "Key \"$resolvedKey\" is already used by ${container::class.simpleName}. " +
            "Pass a unique key to rememberPulseContainer()."
    }
    return container
}

@PublishedApi
@Composable
internal fun rememberPulseStoreHolder(
    key: String,
    factory: () -> PulseStore<*, *, *, *, *>,
): PulseStoreHolder =
    viewModel(
        viewModelStoreOwner = rememberPulseViewModelStoreOwner(),
        key = "jp.kaleidot725.pulse.mvi.PulseStore:$key",
        factory = viewModelFactory { initializer { PulseStoreHolder(factory()) } },
    )

@PublishedApi
@Composable
internal fun rememberPulseContainerHolder(
    key: String,
    factory: () -> PulseContainer<*, *>,
): PulseContainerHolder =
    viewModel(
        viewModelStoreOwner = rememberPulseViewModelStoreOwner(),
        key = "jp.kaleidot725.pulse.mvi.PulseContainer:$key",
        factory = viewModelFactory { initializer { PulseContainerHolder(factory()) } },
    )

/**
 * Returns the host [ViewModelStoreOwner].
 *
 * Android, iOS and Desktop hosts all provide one. Embedding Compose somewhere that does not means
 * there is nothing to own a lifetime, so this fails rather than inventing an owner: a
 * composition scoped stand-in would be created once per call site, putting a Store and its
 * Container in different `ViewModelStore`s and quietly breaking the "unique key per owner" rule.
 */
@PublishedApi
@Composable
internal fun rememberPulseViewModelStoreOwner(): ViewModelStoreOwner =
    checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner in scope. Provide one with " +
            "CompositionLocalProvider(LocalViewModelStoreOwner provides owner), or drive the Store " +
            "lifecycle yourself with the pulsemvi artifact alone."
    }

@PublishedApi
internal class PulseStoreHolder(
    public val store: PulseStore<*, *, *, *, *>,
) : ViewModel() {
    init {
        store.onSetup()
    }

    override fun onCleared() {
        store.close()
    }
}

@PublishedApi
internal class PulseContainerHolder(
    public val container: PulseContainer<*, *>,
) : ViewModel() {
    override fun onCleared() {
        container.close()
    }
}

/**
 * The [NavEntryDecorator] list `NavDisplay` needs for PulseMVI Stores to be scoped to a back stack
 * entry.
 *
 * `NavDisplay` defaults `entryDecorators` to the saveable state holder alone, so passing the
 * ViewModel decorator on its own would drop saveable state. This keeps both:
 *
 * ```kotlin
 * NavDisplay(
 *     backStack = backStack,
 *     entryDecorators = rememberPulseNavEntryDecorators(),
 *     entryProvider = entryProvider { ... },
 * )
 * ```
 *
 * A Store created with [rememberPulseStore] inside a destination then lives exactly as long as its
 * route stays on the back stack.
 */
@Composable
public fun <T : Any> rememberPulseNavEntryDecorators(): List<NavEntryDecorator<T>> =
    listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
    )
