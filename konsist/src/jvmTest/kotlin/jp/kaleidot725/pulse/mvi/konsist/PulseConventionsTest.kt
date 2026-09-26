package jp.kaleidot725.pulse.mvi.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.core.exception.KoAssertionFailedException
import org.junit.Test
import kotlin.test.assertFailsWith

/**
 * The assertions this artifact publishes, run over code that follows the conventions and code that breaks each one.
 */
class PulseConventionsTest {
    /**
     * The conventions hold for code that follows them, and hold quietly: an assertion that passed here would be worth
     * nothing if it also passed on the violations below.
     */
    @Test
    fun `passes on code that follows the conventions`() {
        conforming.assertPulseMviConventions()
    }

    /**
     * The demo is the reference application, so the conventions are checked against it rather than only against
     * fixtures: a rule that no real code satisfies is a rule nobody will keep.
     */
    @Test
    fun `passes on the demo application`() {
        Konsist.scopeFromProject(moduleName = "demo").assertPulseMviConventions()
    }

    /**
     * A project that has not adopted PulseMVI anywhere must pass rather than fail on empty filters, since the
     * assertions are meant to be added before the code they check.
     */
    @Test
    fun `passes on a project with no PulseMVI types`() {
        Konsist.scopeFromDirectory("konsist/src/jvmMain").assertPulseMviConventions()
    }

    /**
     * A state without `data` is the mistake with the least visible consequence — the screen keeps working and only
     * recomposes more than it needs to — so it is the one worth catching mechanically.
     */
    @Test
    fun `reports a state that is not a data class`() {
        val failure = assertFailsWith<KoAssertionFailedException> { violating.assertStatesAreDataTypes() }
        assertMessageMentions(failure, "PlainState")
    }

    /**
     * An unsealed message type compiles, and every `when` over it then needs an `else` branch that quietly swallows the
     * cases added later.
     */
    @Test
    fun `reports a message interface that is not sealed`() {
        val failure = assertFailsWith<KoAssertionFailedException> { violating.assertMessagesAreSealedOrDataTypes() }
        assertMessageMentions(failure, "OpenAction")
    }

    /**
     * The name is how a reader tells a ViewModel from a Container, since both are `ViewModel` subclasses to the
     * compiler.
     */
    @Test
    fun `reports a ViewModel that is not named ViewModel`() {
        val failure =
            assertFailsWith<KoAssertionFailedException> {
                violating.assertViewModelsAndContainersAreNamedAfterTheirBase()
            }
        assertMessageMentions(failure, "Counter")
    }

    /**
     * A `var` beside the state is data the UI cannot observe, which is why it is reported even though it compiles.
     */
    @Test
    fun `reports a ViewModel with a var property`() {
        val failure = assertFailsWith<KoAssertionFailedException> { violating.assertViewModelsHoldTheirDataInTheirState() }
        assertMessageMentions(failure, "Counter")
    }

    /**
     * A section that binds nothing is a component that ended up in the wrong package, and the mistake is invisible
     * until someone looks for where the ViewModel is read.
     */
    @Test
    fun `reports a section that binds no ViewModel`() {
        val failure = assertFailsWith<KoAssertionFailedException> { violatingViews.assertSectionsBindOneViewModel() }
        assertMessageMentions(failure, "SilentSection")
    }

    /**
     * A component that takes a ViewModel cannot be reused by another section or previewed on its own, which is the whole
     * reason it was split out.
     */
    @Test
    fun `reports a component that takes a ViewModel`() {
        val failure = assertFailsWith<KoAssertionFailedException> { violatingViews.assertComponentsAreStateless() }
        assertMessageMentions(failure, "BoundComponent")
    }

    /**
     * A screen that binds its own ViewModel is how a screen grows into the file that knows everything.
     */
    @Test
    fun `reports a screen that binds a ViewModel`() {
        val failure = assertFailsWith<KoAssertionFailedException> { violatingViews.assertScreensComposeSections() }
        assertMessageMentions(failure, "BindingScreen")
    }

    private fun assertMessageMentions(
        failure: KoAssertionFailedException,
        name: String,
    ) {
        val message = failure.message ?: ""
        check(message.contains(name)) { "Expected the failure to name $name, but it said: $message" }
    }

    private val conforming get() = Konsist.scopeFromDirectory("konsist/src/jvmTest/resources/fixture/conforming")

    private val violating get() = Konsist.scopeFromDirectory("konsist/src/jvmTest/resources/fixture/violating")

    private val violatingViews get() = Konsist.scopeFromDirectory("konsist/src/jvmTest/resources/fixture/violating-view")
}
