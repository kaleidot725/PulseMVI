package jp.kaleidot725.pulse.mvi.navigation3

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseViewModel
import kotlin.reflect.KClass

/**
 * Creates a [PulseViewModel] scoped to the current [ViewModelStoreOwner].
 *
 * The instance is kept in the owner's `ViewModelStore`, so a composition restart does not recreate
 * it: state is preserved and `onSetup()` is not repeated. Its scope is cancelled when the owner is
 * cleared.
 *
 * Which owner is in scope decides how long it lives. Under the host owner that is the whole screen;
 * under a Navigation 3 entry — see [rememberPulseNavEntryDecorators] — it is as long as the route
 * stays on the back stack.
 *
 * [PulseViewModel] is a `ViewModel`, so `viewModel()` and `koinViewModel()` create it just as well.
 * This adds a default [key] of the class's qualified name; pass an explicit one when the same type
 * is used more than once under a single owner.
 */
@Composable
public inline fun <reified VM : PulseViewModel<*, *, *, *, *>> rememberPulseViewModel(
    key: String? = null,
    noinline factory: () -> VM,
): VM = rememberPulseInstance(VM::class, key, fallbackKey = "PulseViewModel", factory)

/**
 * Creates a [PulseContainer] scoped to the current [ViewModelStoreOwner].
 *
 * Keeps the Container's Unicast subscriptions alive across recompositions and composition restarts;
 * `close()` runs when the owner is cleared.
 *
 * [key] defaults to the Container's qualified class name.
 */
@Composable
public inline fun <reified Container : PulseContainer<*, *>> rememberPulseContainer(
    key: String? = null,
    noinline factory: () -> Container,
): Container = rememberPulseInstance(Container::class, key, fallbackKey = "PulseContainer", factory)

@PublishedApi
@Composable
internal fun <T : ViewModel> rememberPulseInstance(
    type: KClass<T>,
    key: String?,
    fallbackKey: String,
    factory: () -> T,
): T =
    viewModel(
        modelClass = type,
        viewModelStoreOwner = rememberPulseViewModelStoreOwner(),
        key = key ?: defaultPulseKey(type, fallbackKey),
        factory = viewModelFactory { addInitializer(type) { factory() } },
    )

/**
 * Returns the host [ViewModelStoreOwner].
 *
 * The Compose Desktop host provides one. Embedding Compose somewhere that does not means there is
 * nothing to own a lifetime, so this fails rather than inventing an owner.
 */
@PublishedApi
@Composable
internal fun rememberPulseViewModelStoreOwner(): ViewModelStoreOwner = requirePulseViewModelStoreOwner(LocalViewModelStoreOwner.current)

@PublishedApi
internal fun requirePulseViewModelStoreOwner(owner: ViewModelStoreOwner?): ViewModelStoreOwner =
    checkNotNull(owner) {
        "No ViewModelStoreOwner in scope. Provide one with " +
            "CompositionLocalProvider(LocalViewModelStoreOwner provides owner), or drive the " +
            "PulseViewModel lifecycle yourself with the pulsemvi artifact alone."
    }

/**
 * The key a ViewModel or Container is stored under when the caller passes none: the qualified
 * class name, falling back to the simple name for local classes and to [fallback] for anonymous
 * ones, which have neither.
 */
@PublishedApi
internal fun defaultPulseKey(
    type: KClass<*>,
    fallback: String,
): String = type.qualifiedName ?: type.simpleName ?: fallback

/**
 * The [NavEntryDecorator] list `NavDisplay` needs for PulseMVI ViewModels to be scoped to a back
 * stack entry.
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
 * A [PulseViewModel] created with [rememberPulseViewModel] inside a destination then lives exactly
 * as long as its route stays on the back stack.
 */
@Composable
public fun <T : Any> rememberPulseNavEntryDecorators(): List<NavEntryDecorator<T>> =
    listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
    )
