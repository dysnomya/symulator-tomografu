package com.github.dysnomya.tomograf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class BresenhamLineTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getLineTest() {
        BresenhamLine line = new BresenhamLine(10, 5, 2, 1);
        System.out.println(line.getLine());
    }
}