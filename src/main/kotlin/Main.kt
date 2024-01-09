import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Crop54
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
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
    val authenticators = mutableListOf<Authenticator>()
    fun add(platform: String, secret: ByteArray) {
        this.authenticators.addLast(
            Authenticator(platform, secret)
        )
    }

    fun compute(auth: Authenticator, timestamp: Date): String {
        return auth.authenticator.generate(timestamp)
    }
}

object AppFonts {
    object HarmonyOSSans {
        val SC = FontFamily(
            Font("font/HarmonyOS_Sans_SC_Black.ttf", FontWeight.Black),
            Font("font/HarmonyOS_Sans_SC_Bold.ttf", FontWeight.Bold),
            Font("font/HarmonyOS_Sans_SC_Medium.ttf", FontWeight.Medium),
            Font("font/HarmonyOS_Sans_SC_Regular.ttf", FontWeight.Normal),
        )
    }
}

@Composable
@Preview
fun App(windowState: WindowState) {
    val app = remember { mutableStateOf(Screen.Home) }
    var screen by app
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    val dialogState = remember { mutableStateOf(false) }
    var dialog by dialogState
    val totp = TOTP()
    totp.add("Google", "K6IPBKCQTVLCZDM2".toByteArray())
    totp.add("Facebook", "K6IPBHEQTVLCZDM2".toByteArray())
    totp.add("Twitter", "K6IPBHCQEVLCZDM2".toByteArray())
    totp.add("OpenAI", "K6IPBHCQTVLCEDM2".toByteArray())
    totp.add("Github", "K6IPBHCQTVLCZDM2".toByteArray())
    for (i in 0..150) {
        totp.add("MoChat", "K6IPBHCQTVKCZDM2".toByteArray())
    }

    Scaffold(
        bottomBar = {
            if (windowState.size.width <= 450.dp) {
                NavigationBar {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (screen == Screen.Home) {
                                    Icons.Filled.Home
                                } else {
                                    Icons.Outlined.Home
                                },
                                contentDescription = "Home"
                            )
                        },
                        label = {
                            Text(
                                text = "Home",
                                fontSize = 12.sp,
                                fontFamily = AppFonts.HarmonyOSSans.SC,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        selected = screen == Screen.Home,
                        onClick = {
                            screen = Screen.Home
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (screen == Screen.Search) {
                                    Icons.Filled.Search
                                } else {
                                    Icons.Outlined.Search
                                },
                                contentDescription = "Search"
                            )
                        },
                        label = {
                            Text(
                                text = "Search",
                                fontSize = 12.sp,
                                fontFamily = AppFonts.HarmonyOSSans.SC,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        selected = screen == Screen.Search,
                        onClick = {
                            screen = Screen.Search
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (screen == Screen.Settings) {
                                    Icons.Filled.Settings
                                } else {
                                    Icons.Outlined.Settings
                                },
                                contentDescription = "Settings"
                            )
                        },
                        label = {
                            Text(
                                text = "Settings",
                                fontSize = 12.sp,
                                fontFamily = AppFonts.HarmonyOSSans.SC,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        selected = screen == Screen.Settings,
                        onClick = {
                            screen = Screen.Settings
                        }
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snack)
        },
        floatingActionButton = {
            if (screen == Screen.Home) {
                LargeFloatingActionButton(
                    onClick = {
                        dialog = true
                    }
                ) {
                    Icon(
                        Icons.Filled.Add,
                        "Add a new auth.",
                        modifier = Modifier
                            .size(50.dp)
                    )
                }
            }
        }
    ) {
        Row {
            if (windowState.size.width > 450.dp) {
                NavigationRail {
                    NavigationRailItem(
                        icon = {
                            Icon(
                                if (screen == Screen.Home) {
                                    Icons.Filled.Home
                                } else {
                                    Icons.Outlined.Home
                                },
                                contentDescription = "Home"
                            )
                        },
                        label = {
                            Text(
                                text = "Home",
                                fontSize = 12.sp,
                                fontFamily = AppFonts.HarmonyOSSans.SC,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        selected = screen == Screen.Home,
                        onClick = {
                            screen = Screen.Home
                        }
                    )
                    NavigationRailItem(
                        icon = {
                            Icon(
                                if (screen == Screen.Search) {
                                    Icons.Filled.Search
                                } else {
                                    Icons.Outlined.Search
                                },
                                contentDescription = "Search"
                            )
                        },
                        label = {
                            Text(
                                text = "Search",
                                fontSize = 12.sp,
                                fontFamily = AppFonts.HarmonyOSSans.SC,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        selected = screen == Screen.Search,
                        onClick = {
                            screen = Screen.Search
                        }
                    )
                    NavigationRailItem(
                        icon = {
                            Icon(
                                if (screen == Screen.Settings) {
                                    Icons.Filled.Settings
                                } else {
                                    Icons.Outlined.Settings
                                },
                                contentDescription = "Settings"
                            )
                        },
                        label = {
                            Text(
                                text = "Settings",
                                fontSize = 12.sp,
                                fontFamily = AppFonts.HarmonyOSSans.SC,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        selected = screen == Screen.Settings,
                        onClick = {
                            screen = Screen.Settings
                        }
                    )
                }
            }

            when (screen) {
                Screen.Home -> Home(totp, dialogState)
                Screen.Search -> Search()
                Screen.Settings -> Settings()
            }
        }
    }
}

@Composable
@Preview
fun Home(t: TOTP, dialogState: MutableState<Boolean>) {
    var totp by remember { mutableStateOf("") }
    var time by remember { mutableFloatStateOf(0f) }
    var inputText by remember { mutableStateOf("") }
    var dialog by dialogState
    val scroll = rememberScrollState()

    LaunchedEffect(Unit) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                val timestamp = Date(System.currentTimeMillis())
                val passcode = mutableListOf<String>()
                for (auth in t.authenticators) {
                    passcode.addLast(
                        "${auth.platform}\n${t.compute(auth, timestamp)}"
                    )
                }
                totp = passcode.joinToString("\n\n")
            }
        }, 0, 1000)
    }

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
                time = ((endEpochMillis - Date(System.currentTimeMillis()).time) / 1000).toFloat()
            }
        }, 0, 100)
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
        ) {
            if (totp.isNotEmpty()) {
                for (p in totp.split("\n\n")) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {

                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp, 0.dp)
                        ) {
                            Box {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(0.dp, 12.dp)
                                ) {
                                    Text(
                                        text = p.split("\n")[0],
                                        modifier = Modifier,
                                        color = Color.Black,
                                        fontSize = 30.sp,
                                        fontFamily = AppFonts.HarmonyOSSans.SC,
                                        fontWeight = FontWeight.Black,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = p.split("\n")[1],
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 30.sp,
                                        fontFamily = AppFonts.HarmonyOSSans.SC,
                                        fontWeight = FontWeight.Bold,
                                        overflow = TextOverflow.Visible,
                                        maxLines = 1
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .align(Alignment.CenterEnd)
                                        .padding(12.dp, 0.dp)
                                ) {
                                    CircularProgressIndicator(
                                        progress = time / 30,
                                        modifier = Modifier
                                            .size(24.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 12.dp,
                                        trackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                }
                            }
                            Divider(
                                thickness = 1.dp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }

    if (dialog) {
        Dialog(
            onDismissRequest = {
                dialog = false
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
                            .background(MaterialTheme.colorScheme.background)
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


@Composable
@Preview
fun Title() {
    Row {
        Text(
            text = "MoAuthenticator",
            fontSize = 16.sp,
            fontFamily = AppFonts.HarmonyOSSans.SC,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Clip,
            maxLines = 1
        )
        Card(
            modifier = Modifier
                .padding(6.dp, 0.dp)
                .align(Alignment.CenterVertically),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "内测版",
                modifier = Modifier
                    .padding(6.dp, 0.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontFamily = AppFonts.HarmonyOSSans.SC,
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
        }
    }

}

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val undecoratedState = remember { mutableStateOf(true) }
    val transparentState = remember { mutableStateOf(true) }
    val windowState = rememberWindowState(
        position = WindowPosition(
            x = 100.dp,
            y = 100.dp
        ),
        width = 400.dp,
        height = 600.dp
    )
    val undecorated by undecoratedState
    val transparent by transparentState

    var inMinimize by remember { mutableStateOf(false) }
    var inMaximize by remember { mutableStateOf(false) }
    var inExit by remember { mutableStateOf(false) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "MoAuthenticator",
        icon = painterResource("icon.png"),
        state = windowState,
        undecorated = undecorated,
        transparent = transparent,
    ) {
        MaterialTheme {
            Card(
                shape = if (undecorated) {
                    MaterialTheme.shapes.extraLarge
                } else {
                    MaterialTheme.shapes.medium
                }
            ) {
                Column {
                    WindowDraggableArea(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        if (windowState.placement == WindowPlacement.Floating) {
                                            windowState.placement = WindowPlacement.Fullscreen
                                        } else {
                                            windowState.placement = WindowPlacement.Floating
                                        }
                                    }
                                )
                            }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .padding(24.dp, 6.dp)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                            ) {
                                Title()
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                            ) {
                                Row {
                                    Box(
                                        modifier = Modifier
                                            .padding(6.dp, 0.dp)
                                            .align(Alignment.CenterVertically)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                windowState.isMinimized = true
                                            },
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .onPointerEvent(PointerEventType.Enter) {
                                                    inMinimize = true
                                                }
                                                .onPointerEvent(PointerEventType.Exit) {
                                                    inMinimize = false
                                                },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = Color(0xFF3DE1AD),
                                            )
                                        ) {
                                            if (inMinimize) {
                                                Icon(
                                                    Icons.Outlined.Remove,
                                                    contentDescription = "Minimize",
                                                    modifier = Modifier
                                                        .size(12.dp),
                                                    tint = Color.Black
                                                )
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .padding(6.dp, 0.dp)
                                            .align(Alignment.CenterVertically)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (windowState.placement == WindowPlacement.Floating) {
                                                    windowState.placement = WindowPlacement.Fullscreen
                                                } else {
                                                    windowState.placement = WindowPlacement.Floating
                                                }
                                            },
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .onPointerEvent(PointerEventType.Enter) {
                                                    inMaximize = true
                                                }
                                                .onPointerEvent(PointerEventType.Exit) {
                                                    inMaximize = false
                                                },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = Color(0xFFFF7500),
                                            )
                                        ) {
                                            if (inMaximize) {
                                                Icon(
                                                    Icons.Outlined.Crop54,
                                                    contentDescription = "Maximize",
                                                    modifier = Modifier
                                                        .size(12.dp),
                                                    tint = Color.Black
                                                )
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .padding(6.dp, 0.dp)
                                            .align(Alignment.CenterVertically)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                exitApplication()
                                            },
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .onPointerEvent(PointerEventType.Enter) {
                                                    inExit = true
                                                }
                                                .onPointerEvent(PointerEventType.Exit) {
                                                    inExit = false
                                                },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = Color(0xFFFF00056),
                                            )
                                        ) {
                                            if (inExit) {
                                                Icon(
                                                    Icons.Outlined.Close,
                                                    contentDescription = "Exit",
                                                    modifier = Modifier
                                                        .size(12.dp),
                                                    tint = Color.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    App(windowState)
                }
            }
        }
    }
}
