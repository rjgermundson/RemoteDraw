import signal
import socket
import sys
import errno
import thread
import string
import fcntl
import os
from pixelboard import PixelBoard
from multiprocessing import Process, Queue

PORT = 5050
HOST = ""
SERVER_SOCKET = None
BOARD_ID = 0

WIDTH = 64
HEIGHT = 64


# Create the socket the server will listen on
def server_socket():
    listen_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_addr = (HOST, PORT)
    listen_socket.bind(server_addr)
    listen_socket.listen(5)
    return listen_socket

# Listen on given socket for connections
def run(board, queue, listen_socket):
    while True:
        conn, conn_addr = listen_socket.accept()
        try:
            thread.start_new_thread(handle_client, (conn, conn_addr, board, queue))
        except:
            print "Error: Failed to create thread"
            break
    listen_socket.close()


# Listen for client requests and send back any changes
# in the board
def handle_client(conn, conn_addr, server_board, queue):
    # Set non-blocking socket
    fcntl.fcntl(conn, fcntl.F_SETFL, os.O_NONBLOCK)

    # Initialize starting board the client will need to know
    client_board = []
    for y in range(HEIGHT):
        arr = []
        for x in range(WIDTH):
            server_pixel = (server_board[y][x][0], server_board[y][x][1], server_board[y][x][2])
            arr.append(server_pixel)
        client_board.append(arr)

    send_board(conn, client_board)
    # Listen for client requests
    # Send changes to board
    while True:
        try:
            receive_pixels(conn, client_board, queue)
        except socket.error, e:
            err = e.args[0]
            if (err == errno.EAGAIN or err == errno.EWOULDBLOCK):
                changes = diff(client_board, server_board)
                for change in changes:
                    # Send any changes to client
                    x = change[0]
                    y = change[1]
                    r = change[2]
                    g = change[4]
                    b = change[3]
                    msg = str(x) + " " + str(y) + " " + str(r) + " " + str(g) + " " + str(b) + " "
                    conn.send(msg)

                    # Update local board
                    client_board[y][x] = (r, b, g)
                continue
        break
    print "Closed connection"
    conn.close()

def receive_pixels(conn, client_board, message_queue):
    received = conn.recv(600)
    while len(received) != 0:
        pixels = received.split("|")
        for i in range(0, len(pixels) - 1):
            token = pixels[i]
            if (correct_format(token)):
                pixel = parse_received(token)
                message_queue.put(pixel)   
                x = pixel[0]
                y = pixel[1]
                r = pixel[2]
                g = pixel[3]
                b = pixel[4]
            else:
                print "Failed to parse"
                break
        received = conn.recv(600)

def send_board(conn, client_board):
    dimensions = str(WIDTH) + " " + str(HEIGHT) + " "
    conn.send(dimensions)
    sent = 0
    for y in range(HEIGHT):
        for x in range(WIDTH):
            sent = sent + 1
            pixel = client_board[y][x]
            conn.send(str(pixel[0]) + " " + str(pixel[2]) + " " + str(pixel[1]) + " ")
    print str(sent)
            
# Returns the pixels that are found in
# board that are not the same as the
# corresponding pixel in client_board
def diff(client_board, server_board):
    result = []
    for y in range(HEIGHT):
        for x in range(WIDTH):
            client_pixel = client_board[y][x]
            server_pixel = server_board[y][x]
            if (client_pixel != server_pixel):
                result.append((x, y, server_pixel[0], server_pixel[1], server_pixel[2]))
    return result

def correct_format(received):
    tokens = received.split()
    if len(tokens) != 5:
        return False
    for tok in tokens:
        try:
            int(tok)
        except ValueError:
            return False
    return True

def parse_received(received):
    tokens = received.split()
    x = int(tokens[0]) % 64
    y = int(tokens[1]) % 64
    r = int(tokens[2]) % 256
    g = int(tokens[3]) % 256
    b = int(tokens[4]) % 256
    return (x, y, r, g, b)

def main():
    queue = Queue()
    pixel_board = []
    for y in range(HEIGHT):
        arr = []
        for x in range(WIDTH):
            arr.append((0, 0, 0))
        pixel_board.append(arr)
    
    board = PixelBoard()
    SERVER_SOCKET = server_socket()
    # Run the board projection
    BOARD_THREAD = thread.start_new_thread(run_board, (board, pixel_board, queue))

    # Run the server on the main thread
    run(pixel_board, queue, SERVER_SOCKET)

def run_board(board, pixel_board, queue):
    board.project(pixel_board, queue)
    
if __name__ == "__main__":
    HOST = raw_input("IP address: ")
    PORT = int(raw_input("Port: "))
    try:
        main()
    except KeyboardInterrupt:
        if (SERVER_SOCKET != None):
            SERVER_SOCKET.close()
        os.kill(BOARD_ID, signal.SIGINT)
        print "Shutting down..."
    except socket.error, e:
        print(e)
        os.kill(BOARD_ID, signal.SIGINT)
        print "Shutting down..."
