package parcel

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
actual annotation class Parcelize()
