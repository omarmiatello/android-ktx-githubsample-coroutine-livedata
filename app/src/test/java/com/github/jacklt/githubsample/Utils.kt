package com.github.jacklt.githubsample

import java.io.File

inline fun mockData(filename: String) = File("mockdata/$filename").readText()