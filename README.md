# Aphel Programming Language
![Version](https://img.shields.io/badge/version-1.0.0-blue.svg) ![Java Interpreter](https://img.shields.io/badge/runs%20on-Java-yellow.svg) ![Known Issues](https://img.shields.io/badge/issues-strings%2C%20functions%2C%20logic-red.svg) ![Status](https://img.shields.io/badge/status-experimental-magenta)
```txt
PRINTC("Hello, World! \n")
```
**Aphel** is a simple, high-level, dynamically typed language that I've been building as a fun side project. It's still in early stages, but it's functional enough to handle variable assignments, loops, conditionals, and a bit more :)
Aphel follows an **imperative** style (structured, step-by-step programming), and run scripts directly in your terminal (or CMD)

<sub>Bear in mind that Aphel is still under heavy development, so expect some bugs along the way</sub>

## 🔰 Table of Contents
- [Features](#-features)
- [Installation](#-installation)
- [Code Snippets](#-code-snippets)
- [Known Issues](#-known-issues)
## ✅ Features

- **Dynamic Typing**: Variables do not require explicit type declarations
- **Imperative Paradigm**: Supports structured programming with clear control flow (e.g., loops, conditionals)
- **Basic Input/Output**: Print values using `PRINTC` with dynamic expressions
- **Control Flow**: Includes `IF` statements, `WHILE` loops, and `FOR` loops
- **Basic Arithmetic**: Perform calculations and store results in variables

---

## ⏬ Installation

To run Aphel, ensure you have Java installed. Clone this repository and use the interpreter:

<sub>By default, the repository comes with a HelloWorld.aph file, try it!</sub>
```bash
git clone https://github.com/arminosz/aphel.git
cd aphel
#If in windows CMD:
aphel yourfile.aph
#Other OSs
java -cp src\aphelios\Aphel.jar aphelios.Aphel yourfile.aph
```
<sub>For devs: There's a build.bat in src/aphelios, if you want to build easier :)</sub>


## 🏴 Code Snippets

### 1. **Variable Assignment and Printing**

```txt
x = 10;
y = 20;
z = x + y;
PRINTC("Sum of x and y is: ", z);
```

### 2. **If Statement**

```txt
x = 10;
IF(x > 5) {
    PRINTC("x is greater than 5");
}
```

### 3. **While Loop**

```txt
i = 0;
WHILE(i < 5) {
    PRINTC(i, " ");
    i = i + 1;
}
```

### 4. **For Loop**

```txt
FOR(i = 0; i < 5; i = i + 1) {
    PRINTC("Iteration: ", i, "\n");
}
```

### 5. **Function Definition and Call**

```txt
FUNCTION add(a, b) {
    RETURN a + b;
}

result = add(5, 10);
PRINTC("Result of function call: ", result);
```
> You might get an error here, functions aren’t fully ready yet

### 6. **String Manipulation**

```txt
name = "John";
greeting = "Hello, " + name + "!";
PRINTC(greeting);
```
> Your output may not be the expected one, string manipulation isn't fully ready yet!


## ⚡ Known Issues

The current version of Aphel is still under development, and some features may not work as expected. Here are a few known issues:

- **String Manipulation**: Variables containing strings may not behave as expected when manipulated
- **Functions**: General function handling is still in development, with incomplete functionality
- **Division Results**: Any division operation that would result in a floating-point number is rounded to the nearest integer
- **Logical Operations**: Operations like `&&` (AND) and `||` (OR) are not functioning correctly

---
