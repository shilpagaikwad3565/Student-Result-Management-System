package com.movieticket.main;

import com.movieticket.dao.*;
import com.movieticket.model.*;

import java.sql.Timestamp;
import java.util.InputMismatchException; 
import java.util.List;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static UserDao userDao = new UserDao();
    static MovieDao movieDao = new MovieDao();
    static ShowDao showDao = new ShowDao();
    static BookingDao bookingDao = new BookingDao();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\nWelcome to the Movie Ticket Reservation System");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Forgot Password"); 
            System.out.println("4. View Movies");
            System.out.println("5. Book Ticket");
            System.out.println("6. View Bookings");
            System.out.println("7. Cancel Booking");
            System.out.println("8. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    register();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    forgotPassword();
                    break;
                case 4:
                    viewMovies();
                    break;
                case 5:
                    bookTicket();
                    break;
                case 6:
                    viewBookings();
                    break;
                case 7:
                    cancelBooking();
                    break;
                case 8:
                    System.out.println("Thankuuu...!!");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static void register() {
        System.out.print("\nEnter username: ");
        String username = scanner.nextLine();

        if (userDao.isUsernameExists(username)) {
            System.out.println("Username is already taken. Please choose another one.");
            return; 
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        if (!isValidEmail(email)) {
            System.out.println("Invalid email format.");
            return;
        }

        if (userDao.registerUser(new User(0, username, password, email))) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Registration failed. Please try again.");
        }
    }

    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return email.matches(emailRegex);
    }

    public static void login() {
        System.out.println("\nEnter username:");
        String username = scanner.nextLine();
        System.out.println("Enter password:");
        String password = scanner.nextLine();

        User user = userDao.loginUser(username, password);
        if (user != null) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    public static void forgotPassword() {
        System.out.print("\nEnter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your new password: ");
        String newPassword = scanner.nextLine();

        if (userDao.updatePassword(username, newPassword)) { 
            System.out.println("Password updated successfully!");
        } else {
            System.out.println("Failed to update password. User not found or other issue.");
        }
    }

    public static void viewMovies() {
        List<Movie> movies = movieDao.getAllMoviesWithShowDetails();
        if (movies.isEmpty()) {
            System.out.println("No movies available.");
        } else {
            for (Movie movie : movies) {
                // Display movie information along with show details
                System.out.println("\nName: " + movie.getMovieName() +
                                   " | Duration: " + movie.getDuration() + " mins" +
                                   " | Genre: " + movie.getGenre());

                // Display associated shows
                List<Show> shows = movie.getShows();
                if (shows != null && !shows.isEmpty()) {
                    for (Show show : shows) {
                        System.out.println("  Show: " + show.getShowName() +
                                           " | Time: " + show.getShowTime() );
                                           
                    }
                } else {
                    System.out.println("  No shows available for this movie.");
                }
            }
        }
    }

    public static void bookTicket() {
        System.out.print("\nEnter your username: ");
        String username = scanner.nextLine();

        User user = userDao.getUserByUsername(username);
        if (user == null) {
            System.out.println("User not found. Please register or check your username.");
            return;
        }

        // Display all available movies
        List<Movie> movies = movieDao.getAllMovies();  
        if (movies.isEmpty()) {
            System.out.println("No movies available.");
            return;
        }

        System.out.println("Available Movies:");
        for (int i = 0; i < movies.size(); i++) {
            System.out.println((i + 1) + ". " + movies.get(i).getMovieName() + " | Genre: " + movies.get(i).getGenre() + " | Duration: " + movies.get(i).getDuration() + " minutes");
        }

        System.out.print("\nSelect a movie by entering the corresponding number: ");
        int movieChoice;
        try {
            movieChoice = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine();
            return;
        }

        if (movieChoice < 1 || movieChoice > movies.size()) {
            System.out.println("Invalid choice. Please try again.");
            return;
        }

        Movie selectedMovie = movies.get(movieChoice - 1);  
        String movieName = selectedMovie.getMovieName();
        int movieId = selectedMovie.getMovieId(); // Get the movie ID

        // Display available shows for the selected movie
        List<Show> shows = showDao.getShowsByMovieId(movieId);
        if (shows.isEmpty()) {
            System.out.println("No shows available for this movie.");
            return;
        }

        System.out.println("Available Shows for " + movieName + ":");
        for (Show show : shows) {
            System.out.println(show.getShowName() + " | Time: " + show.getShowTime());
        }

        System.out.print("\nSelect show time by entering the show name: ");
        String showName = scanner.nextLine();

        System.out.print("\nEnter number of tickets: ");
        int noOfTickets;
        try {
            noOfTickets = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid number of tickets.");
            scanner.nextLine();
            return;
        }

        // Retrieve the show based on selected show name
        Show selectedShow = showDao.getShowByMovieAndName(movieId, showName);
        if (selectedShow == null) {
            System.out.println("Show not found. Please check the show name.");
            return;
        }

        if (selectedShow.getAvailableSeats() < noOfTickets) {
            System.out.println("Not enough available seats.");
            return;
        }

        Booking booking = new Booking(0, user.getUserId(), user.getUsername(), movieName, selectedShow.getShowId(), selectedShow.getShowName(), noOfTickets);

        if (bookingDao.bookTicket(booking)) {
            System.out.println("Booking successful!");
            System.out.println("\nBooking ID: " + booking.getBookingId() +
                               " | Movie Name: " + booking.getMovieName() +
                               " | Show Name: " + booking.getShowName() +
                               " | Tickets: " + booking.getNoOfTickets());
        } else {
            System.out.println("Booking failed.");
        }
    }    
    public static void viewBookings() {
        System.out.print("\nEnter your username: ");
        String username = scanner.nextLine();
        
        List<Booking> bookings = bookingDao.getBookingsByUsername(username);
        
        if (bookings.isEmpty()) {
            System.out.println("No bookings found for the user: " + username);
        } else {
            for (Booking booking : bookings) {
                System.out.println("Booking ID: " + booking.getBookingId() + 
                                   " | Movie Name: " + booking.getMovieName() + 
                                   " | Show Name: " + booking.getShowName() + 
                                   " | Tickets: " + booking.getNoOfTickets());
            }
        }
    }

    public static void cancelBooking() {
        System.out.print("\nEnter booking ID: ");
        int bookingId = scanner.nextInt();
        if (bookingDao.cancelBooking(bookingId)) {
            System.out.println("Booking canceled successfully.");
        } else {
            System.out.println("Failed to cancel booking.");
        }
    }
}
