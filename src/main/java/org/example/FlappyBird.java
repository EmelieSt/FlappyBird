package org.example;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // bilder
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // fågelklass
    int birdX = boardWidth/8;
    int birdY = boardWidth/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // rörklass
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  // skala med 1/6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // spelloop
    Bird bird;
    int velocityX = -4; // rörelsehastighet för rören åt vänster (simulerar fågelns rörelse åt höger)
    int velocityY = 0; // rörelsehastighet för fågeln upp/ned
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // ladda bilder
        backgroundImg = new ImageIcon(getClass().getResource("/img/flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("/img/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("/img/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("/img/bottompipe.png")).getImage();

        // fågel
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // timer för att placera rör
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Kod som ska köras
                placePipes();
            }
        });
        placePipeTimer.start();

        // speltimer
        gameLoop = new Timer(1000/60, this); // hur lång tid det tar innan timern startar, millisekunder mellan varje bildruta
        gameLoop.start();
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y  + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // bakgrund
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        // fågel
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // rör
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // poäng
        g.setColor(Color.white);

        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        }
        else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }

    }

    public void move() {
        // fågel
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); // förhindrar att fågeln går utanför toppen

        // rör
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; // 0.5 eftersom det finns 2 rör! så 0.5*2 = 1, 1 för varje set av rör
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   // a's övre vänstra hörn når inte b's övre högra hörn
                a.x + a.width > b.x &&   // a's övre högra hörn passerar b's övre vänstra hörn
                a.y < b.y + b.height &&  // a's övre vänstra hörn når inte b's nedre vänstra hörn
                a.y + a.height > b.y;    // a's nedre vänstra hörn passerar b's övre vänstra hörn
    }

    @Override
    public void actionPerformed(ActionEvent e) { // anropas varje x millisekunder av gameLoop-timern
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            // System.out.println("JUMP!");
            velocityY = -9;

            if (gameOver) {
                // starta om spelet genom att återställa villkoren
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
            }
        }
    }

    // inte nödvändigt
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
