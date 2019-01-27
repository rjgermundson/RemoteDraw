import time
import os
from multiprocessing import Queue
from samplebase import SampleBase

class PixelBoard(SampleBase):
    
    def __init__(self, *args, **kwargs):
        super(PixelBoard, self).__init__(*args, **kwargs)

    def receive_pixel(self, queue):
        return queue.get()
    
    def set_pixel(self, board, pixel):
        x = pixel[0]
        y = pixel[1]
        red = pixel[2]
        green = pixel[3]
        blue = pixel[4]
        board[y][x] = (red, blue, green)

    def set_board(self, board):
        self.server_board = board

    def project(self, board, queue):
        print queue
        self.process()
        # Get canvas
        canvas = self.matrix.CreateFrameCanvas()

        # Listen for tuples from server that contain
        # pixel to set
        while True:
            for x in range(0, self.matrix.width):
                for y in range(0, self.matrix.height):
                    pixel = board[y][x]
                    canvas.SetPixel(x, y, pixel[0], pixel[1], pixel[2])


            canvas = self.matrix.SwapOnVSync(canvas)
            pixel = self.receive_pixel(queue)
            self.set_pixel(board, pixel)
        
