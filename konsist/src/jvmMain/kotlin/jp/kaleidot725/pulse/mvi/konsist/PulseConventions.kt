package jp.kaleidot725.pulse.mvi.konsist

import com.lemonappdev.konsist.api.KoModifier
import com.lemonappdev.konsist.api.container.KoScope
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import com.lemonappdev.konsist.api.declaration.KoObjectDeclaration
import com.lemonappdev.konsist.api.ext.list.withParentOf
import com.lemonappdev.konsist.api.verify.assertTrue
import jp.kaleidot725.pulse.mvi.PulseAction
import jp.kaleidot725.pulse.mvi.PulseBroadcast
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseEvent
import jp.kaleidot725.pulse.mvi.PulseState
import jp.kaleidot725.pulse.mvi.PulseUnicast
import jp.kaleidot725.pulse.mvi.PulseViewModel

/**
 * Checks every convention PulseMVI expects of the code that uses it.
 *
 * The compiler already rejects the mistakes it can see: a ViewModel must implement `onAction`, and its message types
 * must be the ones its Container knows. What is left are the habits the types cannot express, and this is what they are
 * checked against.
 *
 * ```kotlin
 * class PulseMviConventionTest {
 *     @Test
 *     fun `follows the PulseMVI conventions`() {
 *         Konsist.scopeFromProduction().assertPulseMviConventions()
 *     }
 * }
 * ```
 *
 * Each check is also public on its own, so a project that disagrees with one can call the rest.
 */
public fun KoScope.assertPulseMviConventions() {
    assertStatesAreDataTypes()
    assertMessagesAreSealedOrDataTypes()
    assertViewModelsAndContainersAreNamedAfterTheirBase()
    assertViewModelsHoldTheirDataInTheirState()
}

/**
 * A [PulseState] is a `data class` or a `data object`.
 *
 * State reaches the UI through a `StateFlow`, which drops an emission equal to the last one, and Compose skips a
 * composable whose arguments are equal to the previous ones. Both need `equals`, so a state without `data` recomposes
 * the screen on every update.
 */
public fun KoScope.assertStatesAreDataTypes() {
    pulseStateClasses().assertTrue(
        testName = "PulseState implementations are data classes",
        additionalMessage = "Add `data` so equal states compare equal and Compose can skip the recomposition.",
    ) { it.hasModifier(KoModifier.DATA) }

    pulseStateObjects().assertTrue(
        testName = "PulseState implementations are data objects",
        additionalMessage = "Add `data` so the object prints and compares as a state rather than as an identity.",
    ) { it.hasModifier(KoModifier.DATA) }
}

/**
 * A [PulseAction], [PulseEvent], [PulseBroadcast] or [PulseUnicast] is a `sealed interface`, or a `data` type when a
 * single case is all there is.
 *
 * Every one of them is consumed by a `when`, in `onAction`, `onReceive`, `onReceived` or an event handler. Sealing the
 * type is what makes that `when` exhaustive, so adding a case turns into a compile error instead of a branch nobody
 * wrote.
 */
public fun KoScope.assertMessagesAreSealedOrDataTypes() {
    pulseMessageInterfaces().assertTrue(
        testName = "PulseMVI message interfaces are sealed",
        additionalMessage = "Add `sealed` so a `when` over this type stays exhaustive as cases are added.",
    ) { it.hasModifier(KoModifier.SEALED) }

    pulseMessageClasses().assertTrue(
        testName = "PulseMVI messages implemented by a class are data classes",
        additionalMessage = "Add `data`, or make the type a sealed interface and put the case inside it.",
    ) { it.hasModifier(KoModifier.DATA) }

    pulseMessageObjects().assertTrue(
        testName = "PulseMVI messages implemented by an object are data objects",
        additionalMessage = "Add `data`, or make the type a sealed interface and put the case inside it.",
    ) { it.hasModifier(KoModifier.DATA) }
}

/**
 * A [PulseViewModel] is named `…ViewModel` and a [PulseContainer] is named `…Container`.
 *
 * Which of the two a class is decides where it lives and how long: a Container coordinates ViewModels and is closed with
 * the store that holds it. The name is what says which one a reader is looking at.
 */
public fun KoScope.assertViewModelsAndContainersAreNamedAfterTheirBase() {
    pulseViewModels().assertTrue(
        testName = "PulseViewModel subclasses are named ViewModel",
        additionalMessage = "Rename the class so it ends with `ViewModel`.",
    ) { it.hasNameEndingWith("ViewModel") }

    pulseContainers().assertTrue(
        testName = "PulseContainer subclasses are named Container",
        additionalMessage = "Rename the class so it ends with `Container`.",
    ) { it.hasNameEndingWith("Container") }
}

/**
 * A [PulseViewModel] and a [PulseContainer] declare no `var` property.
 *
 * Everything the UI reads belongs in the state, where `update` publishes it to the `StateFlow`. A `var` beside the state
 * is data the UI cannot observe: it changes without an emission, and nothing recomposes.
 */
public fun KoScope.assertViewModelsHoldTheirDataInTheirState() {
    (pulseViewModels() + pulseContainers()).assertTrue(
        testName = "PulseMVI ViewModels and Containers keep their data in their state",
        additionalMessage = "Move the `var` into the state and publish it with `update`.",
    ) { declaration -> declaration.properties().none { it.isVar } }
}

/** Every class that extends [PulseViewModel] directly. */
public fun KoScope.pulseViewModels(): List<KoClassDeclaration> = classes().withParentOf(PulseViewModel::class)

/** Every class that extends [PulseContainer] directly. */
public fun KoScope.pulseContainers(): List<KoClassDeclaration> = classes().withParentOf(PulseContainer::class)

/** Every class that implements [PulseState] directly. */
public fun KoScope.pulseStateClasses(): List<KoClassDeclaration> = classes().withParentOf(PulseState::class)

/** Every object that implements [PulseState] directly. */
public fun KoScope.pulseStateObjects(): List<KoObjectDeclaration> = objects().withParentOf(PulseState::class)

/** Every interface that implements one of the four message markers directly. */
public fun KoScope.pulseMessageInterfaces(): List<KoInterfaceDeclaration> = interfaces().withParentOf(messageMarkers)

/** Every class that implements one of the four message markers directly. */
public fun KoScope.pulseMessageClasses(): List<KoClassDeclaration> = classes().withParentOf(messageMarkers)

/** Every object that implements one of the four message markers directly. */
public fun KoScope.pulseMessageObjects(): List<KoObjectDeclaration> = objects().withParentOf(messageMarkers)

private val messageMarkers =
    listOf(
        PulseAction::class,
        PulseEvent::class,
        PulseBroadcast::class,
        PulseUnicast::class,
    )
