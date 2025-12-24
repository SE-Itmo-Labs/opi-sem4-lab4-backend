package com.ssnagin.servlets.coordinates.exceptions

import java.lang.Exception

class PointOutOfBoundariesException(message: String = "Point is out of bounds") : Exception(message)