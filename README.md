# shiirudo [![](https://jitpack.io/v/com.tkhskt/shiirudo.svg)](https://jitpack.io/#com.tkhskt/shiirudo)

Generates DSL to simplify processing branching by when expressions in sealed class/interface

## Setup

Refer to the [KSP quickstart Guide](https://kotlinlang.org/docs/ksp-quickstart.html) to make KSP
available for your project.

### Installation

Repository is now **Jitpack**:

```gradle
repositories {
   maven { url "https://jitpack.io" }
}
```

Check the [latest-version](https://jitpack.io/#com.tkhskt/shiirudo)

```gradle
implementation "com.tkhskt:shiirudo:[latest-version]"
ksp "com.tkhskt:shiirudo:[latest-version]"
```

## Usage

Annotate the target sealed class/interface with the Shiirudo annotation.

```kotlin
@Shiirudo
sealed interface Event {
    object ShowDialog : Event
    object CloseDialog : Event
    object ShowToast : Event
}
```

When the project is built, `shiirudo` methods corresponding to subclasses of the annotated class are
generated.

The shiirudo method can be used as follows.

```kotlin
fun handleEvent(event: Event) {
    event.shiirudo()
        .isShowDialog {

        }
        .isCloseDialog {

        }
        .isElse {

        }.execute()
}

// or

fun handleEvent(event: Event) {
    shiirudo {
        event
    }.isShowDialog {

    }.isCloseDialog {

    }.isElse {

    }.execute()
}

// or

fun handleEvent(event: Event) {
    event.shiirudo {
        isShowDialog {

        }
        isCloseDialog {

        }
        isElse {

        }
    }
}
```

### Use with Flow

You can use shiirudo when you collect Flow as follows.

```kotlin
private suspend fun Flow<Event>.collectEvent(
    handler: EventShiirudoBuilder.() -> Unit,
) {
    collect {
        it.shiirudo(handler)
    }
}

fun handleEvent(eventFlow: Flow<Event>) {
    eventFlow.collectEvent {
        isShowDialog {

        }
        isCloseDialog {

        }
    }
}
```

### Name Resolution

The code generated from the nested sealed class/interface and its subclasses is name resolved as
follows.

```kotlin
class SampleViewModel : ViewModel() {

    @Shiirudo
    sealed interface Event {
        interface Dialog {
            object Show : Event
            object Close : Event
        }
        object ShowToast : Event
    }
}

fun handleEvent(event: SampleViewModel.Event) {
    event.shiirudo {
        isDialogShow {

        }
        isDialogClose {

        }
        isShowToast {

        }
    }
}
```

## Examples

See the [sample](./sample) project

## License

MIT
