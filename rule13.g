//Succeeds
fun int success() {
    if (1) {return 0;}
    return 1;
}

// Fails
fun int fail() {
    if ("hello") {return 0;}
    return 1;
}
