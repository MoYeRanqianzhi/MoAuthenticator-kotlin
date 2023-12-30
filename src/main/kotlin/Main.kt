import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator
import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import java.util.*
import java.util.concurrent.TimeUnit

enum class Screen {
    Home,
    Search,
    Settings
}

data class Authenticator(val platform: String, val secret: ByteArray) {
    val authenticator = GoogleAuthenticator(secret)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Authenticator

        if (platform != other.platform) return false
        if (!secret.contentEquals(other.secret)) return false
        if (authenticator != other.authenticator) return false

        return true
    }

    override fun hashCode(): Int {
        var result = platform.hashCode()
        result = 31 * result + secret.contentHashCode()
        result = 31 * result + authenticator.hashCode()
        return result
    }

}


class TOTP {
    public val authenticators = mutableListOf<Authenticator>()
    fun add(platform: String, secret: ByteArray) {
        this.authenticators.addLast(
            Authenticator(platform, secret)
        )
    }

    fun compute(auth: Authenticator, timestamp: Date): String {
        return auth.authenticator.generate(timestamp)
    }
}

@Composable
@Preview
fun App() {
    var screen by remember { mutableStateOf(Screen.Home) }
    val totp = TOTP()
    totp.add("Google", "K6IPBKCQTVLCZDM2".toByteArray())
    totp.add("Facebook", "K6IPBHEQTVLCZDM2".toByteArray())
    totp.add("Twitter", "K6IPBHCQEVLCZDM2".toByteArray())
    totp.add("OpenAI", "K6IPBHCQTVLCEDM2".toByteArray())
    totp.add("Github", "K6IPBHCQTVLCZDM2".toByteArray())
    totp.add("MoChat", "K6IPBHCQTVKCZDM2".toByteArray())


    MaterialTheme {
        Row {
            NavigationRail {
                NavigationRailItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = screen == Screen.Home,
                    onClick = {
                        screen = Screen.Home
                    }
                )
                NavigationRailItem(
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    label = { Text("Search") },
                    selected = screen == Screen.Search,
                    onClick = {
                        screen = Screen.Search
                    }
                )
                NavigationRailItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = screen == Screen.Settings,
                    onClick = {
                        screen = Screen.Settings
                    }
                )
            }
            when (screen) {
                Screen.Home -> Home(totp)
                Screen.Search -> Search()
                Screen.Settings -> Settings()
            }

        }
    }
}

@Composable
@Preview
fun Home(t: TOTP) {
    var time by remember { mutableIntStateOf(0) }
    var new by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("") }
    var diglog by remember { mutableStateOf(false) }

//    Button(onClick = {
//        text = "Hello, Desktop!"
//    }) {
//        Text(text, color = Color.White)
//    }
    LaunchedEffect(Unit) {
        val config = TimeBasedOneTimePasswordConfig(
            codeDigits = 6,
            hmacAlgorithm = HmacAlgorithm.SHA256,
            timeStep = 30,
            timeStepUnit = TimeUnit.SECONDS
        )
        val timeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator("".toByteArray(), config)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                val counter = timeBasedOneTimePasswordGenerator.counter()
                val endEpochMillis = timeBasedOneTimePasswordGenerator.timeslotStart(counter + 1) - 1
                time = ((endEpochMillis - Date(System.currentTimeMillis()).time) / 1000).toInt()
            }
        }, 0, 100)
    }
    if (new) {
        new = false
        LazyColumn {
            val timestamp = Date(System.currentTimeMillis())
            items(items = t.authenticators) { auth ->
                Text(
                    auth.platform + "    " + t.compute(auth, timestamp) + "    " + time.toString(),
                    color = Color.Black
                )
            }
        }
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    diglog = true
                }
            ) {
                Icon(Icons.Filled.Add, "Add a new auth.")
            }
        }
    ) {}
    if (diglog) {
        Dialog(
            onDismissRequest = {
                diglog = false
                new = true
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = {
                            inputText = it
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotEmpty()) {
                                    t.add("New", inputText.toByteArray())
                                    inputText = ""
                                }
                            }
                        ),
                        modifier = Modifier
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                            .padding(8.dp),
                        shape = RoundedCornerShape(30.dp),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (inputText.isNotEmpty()) {
                                t.add("New", inputText.toByteArray())
                                inputText = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddCircle,
                            contentDescription = "Send",
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
@Preview
fun Search() {
    var text by remember { mutableStateOf("Hello, Search!") }

    Button(onClick = {
        text = "Hello, Desktop!"
    }) {
        Text(text, color = Color.White)
    }
}

@Composable
@Preview
fun Settings() {
    var text by remember { mutableStateOf("Hello, Settings!") }

    Button(onClick = {
        text = "Hello, Desktop!"
    }) {
        Text(text, color = Color.White)
    }
}


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "MoAuthenticator",
        icon = painterResource("icon.png"),
        state = WindowState(position = WindowPosition(x = 490.dp, y = 270.dp), width = 960.dp, height = 540.dp)
    ) {
        App()
    }
}
