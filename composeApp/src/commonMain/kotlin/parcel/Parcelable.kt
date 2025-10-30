package parcel

// Similar rational to Parcelable above
// Unlike Parcebale, the actual implementation on Android is implemented
// via a compiler argument:
//
// freeCompilerArgs.addAll(
//     "-P",
//     "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=parcel.Parcelize",
// )
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class Parcelize()

// Expect/Actual Parcelable is necessary for tagging objects as Parcelable on Android
// but not on other platforms, because the "true" Parcelable interface used by Android
// is only present on Android
expect interface Parcelable
