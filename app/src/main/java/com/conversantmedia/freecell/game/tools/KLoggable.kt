import android.util.Log
import kotlin.properties.Delegates

/**
 * Logging Observer
 *
 * The following is an example of a Logger employing the Observer Design pattern.  The idea is to
 * write logs to any receiver by observing changes to objects and their members.  This is a sane
 * prototype to demonstrate logging which does not require the use of logging strings at the call site.
 */

/**
 * The Listener interface allows the developer to pass an object implementing the listener to
 * a constructor.  The object implementing KLogListener is required to provide a log() function.
 */
interface KLogListener {
    fun log(urgency: KLoggable.KLogUrgency, message: Any, owner: String, receiver: KLoggable.KLogReceiver)
}

/**
 * The loggable class implements the listener and is quite customizable.
 */
open class KLoggable () : KLogListener {

    // In this implementation, the logging function depends on the urgency of the message
    override fun log(urgency: KLogUrgency, message: Any, owner: String, receiver: KLogReceiver) {
        when (urgency) {
            KLogUrgency.DEBUG -> logDebug(owner, message)
            KLogUrgency.ERROR -> logError(owner, message)
            KLogUrgency.WARNING -> logWarning(owner, message)
            KLogUrgency.GENERAL -> logGeneral(owner, message)
        }
    }

    /** Below are four example functions called for various urgency needs.
     *
     * @param owner The calling class or other owner
     * @param message Pass any object with a readied toString()
     */
    private fun logDebug(owner: String, message: Any) {
        Log.d(owner, message.toString())
    }

    private fun logError(owner: String, message: Any) {
        // Send HTTP Post JSON Object to Error Server and/or Console
        // depending on Gradle Build Type, i.e. release, debug, alpha, beta, etc.
        // Beyond the scope of this demonstration, but I recommend using a solid http client
        // like Retrofit
    }


    private fun logWarning(owner: String, message: Any) {
        Log.w(owner, message.toString())
    }

    private fun logGeneral(owner: String, message: Any) {
        Log.i(owner, message.toString())
    }

    // internal enum class to specify generic log receivers
    enum class KLogReceiver {
        CONSOLE,
        NETWORK_SERVICE,
        TEXT_FILE
    }

    // internal enum class to specify message urgency
    enum class KLogUrgency {
        ERROR,
        WARNING,
        DEBUG,
        GENERAL
    }

}

/**
 * This is a simple example of a class which uses an observable logger to note new grocery items.
 * In practice, the same pattern can be used in Exceptions, and database callbacks, as well.
 */
class GroceryItem(logger: KLogListener){
    var item: String by Delegates.observable(
            initialValue = "",
            onChange = {
                _, _, newValue ->
                logger.log(KLoggable.KLogUrgency.GENERAL,
                        newValue,
                        javaClass.simpleName,
                        KLoggable.KLogReceiver.CONSOLE)
            })
}

/**
 * When the grocery item Broccoli is assigned, Log.Info will print to LogCat
 */
fun main() {
    val owner = "main function"
    val logger = KLoggable()
    val grocery = GroceryItem(logger)
    grocery.item = "Broccoli"

}