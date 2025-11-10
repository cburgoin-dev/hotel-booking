package service;

import dao.UserDAO;
import exception.*;
import model.Role;
import model.User;
import util.SecurityUtil;

import java.util.List;
import java.util.logging.Logger;

public class UserService {
    private final static Logger logger = Logger.getLogger(UserService.class.getName());
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public UserService() {
        this(new UserDAO());
    }

    public User getUserById(int id) throws DAOException, NotFoundException {
        return userDAO.findById(id);
    }

    public User getUserByEmail(String email) throws DAOException, NotFoundException {
        return userDAO.findByEmail(email);
    }

    public List<User> getAllUsers() throws DAOException {
        return userDAO.getAll();
    }

    public void createUser(User user) throws DAOException, InvalidException, InvalidEmailException, InvalidPasswordException, InvalidRoleException, EmailAlreadyExistsException {
        validateUser(user);

        String password = user.getPasswordHash();
        if (!password.startsWith("$2a$")) {
            user.setPasswordHash(SecurityUtil.hashPassword(password));
        }

        userDAO.insert(user);
        logger.info("User created successfully: userId=" + user.getId());
    }

    public void updateUser(User user) throws DAOException, NotFoundException, InvalidException, InvalidEmailException, InvalidPasswordException, InvalidRoleException, EmailAlreadyExistsException {
        logger.info("Attempting to update user: userId=" + user.getId());
        validateUser(user);

        String password = user.getPasswordHash();
        if (!password.startsWith("$2a$")) {
            user.setPasswordHash(SecurityUtil.hashPassword(password));
        }

        userDAO.update(user);
        logger.info("User updated successfully: userId=" + user.getId());
    }

    public void deleteUser(int id) throws DAOException, NotFoundException {
        logger.info("Attempting to delete user: userId=" + id);
        userDAO.delete(id);
        logger.info("User deleted successfully: userId=" + id);
    }

    private void validateUser(User user) throws InvalidException, InvalidEmailException, InvalidPasswordException, InvalidRoleException, EmailAlreadyExistsException {
        if (user == null) {
            logger.warning("Invalid user: user is null");
            throw new InvalidException("User");
        }

        validateEmail(user.getEmail());
        validatePassword(user.getPasswordHash());
        validateRole(user.getRole().name());

        if (isEmailDuplicate(user.getEmail(), user.getId())) {
            logger.warning("Invalid user email: email already exists");
            throw new EmailAlreadyExistsException();
        }
    }

    private void validateEmail(String email) throws InvalidEmailException {
        if (email == null || email.isBlank()) {
            logger.warning("Invalid user email: email is null or empty");
            throw new InvalidEmailException(true);
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            logger.warning("Invalid user email: email format is not valid");
            throw new InvalidEmailException(false);
        }
    }

    private void validatePassword(String password) throws InvalidPasswordException {
        if (password == null || password.isBlank()) {
            logger.warning("Invalid user password: password is null or empty");
            throw new InvalidPasswordException(InvalidPasswordException.Reason.EMPTY);
        }
        if (password.length() < 8) {
            logger.warning("Invalid user password: password must be at least 8 characters long");
            throw new InvalidPasswordException(InvalidPasswordException.Reason.TOO_SHORT);
        }
        if (!password.matches(".*[A-Z].*")) {
            logger.warning("Invalid user password: password must contain at least one uppercase letter");
            throw new InvalidPasswordException(InvalidPasswordException.Reason.NO_UPPERCASE);
        }
        if (!password.matches(".*\\d.*")) {
            logger.warning("Invalid user password: Password must contain at least one number");
            throw new InvalidPasswordException(InvalidPasswordException.Reason.NO_NUMBER);
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            logger.warning("Invalid user password: Password must contain at least one special character");
            throw new InvalidPasswordException(InvalidPasswordException.Reason.NO_SPECIAL_CHAR);
        }
    }

    private void validateRole(String role) throws InvalidRoleException {
        if (role == null || role.isBlank()) {
            logger.warning("Invalid user role: role is null or empty");
            throw new InvalidRoleException(true, role);
        }
        try {
            Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid user role: " + role);
            throw new InvalidRoleException(false, role);
        }
    }

    private boolean isEmailDuplicate(String email, Integer id) throws DAOException {
        return userDAO.existsByEmail(email, id);
    }

    public User authenticate(String email, String password) throws DAOException, NotFoundException, InvalidEmailException, InvalidPasswordException, UserInactiveException {
        logger.info("Attempting to authenticate user with email: " + email);

        validateEmail(email);
        validatePassword(password);

        User user = userDAO.findByEmail(email);
        boolean passwordMatches = user.checkPassword(password);
        if (!passwordMatches) {
            logger.warning("Authentication failed: incorrect password for userId=" + user.getId());
            throw new InvalidPasswordException(InvalidPasswordException.Reason.INVALID);
        }

        if (!user.isActive()) {
            logger.warning("Authentication failed: user account is inactive (userId=" + user.getId() + ")");
            throw new UserInactiveException();
        }

        logger.info("Authentication successful for userId=" + user.getId());
        return user;
    }
}
