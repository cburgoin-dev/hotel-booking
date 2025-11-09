package service;

import dao.GuestDAO;
import exception.*;
import model.Guest;

import java.util.List;
import java.util.logging.Logger;

public class GuestService {
    private final static Logger logger = Logger.getLogger(GuestService.class.getName());
    private final GuestDAO guestDAO;

    public GuestService(GuestDAO guestDAO) {
        this.guestDAO = guestDAO;
    }

    public GuestService() {
        this(new GuestDAO());
    }

    public Guest getGuestById(int id) throws DAOException, NotFoundException {
        return guestDAO.findById(id);
    }

    public Guest getGuestByName(String name) throws DAOException, NotFoundException {
        return guestDAO.findByName(name);
    }

    public Guest getGuestByEmail(String name) throws DAOException, NotFoundException {
        return guestDAO.findByEmail(name);
    }

    public List<Guest> getAllGuests() throws DAOException {
        return guestDAO.getAll();
    }

    public void createGuest(Guest guest) throws DAOException, GuestInvalidException, GuestNameEmptyException, GuestEmailInvalidException, GuestEmailAlreadyExistsException, GuestPhoneInvalidException {
        logger.info("Attempting to create guest: guestId=" + guest.getId());
        validateGuest(guest);
        guestDAO.insert(guest);
        logger.info("Guest created successfully: guestId=" + guest.getId());
    }

    public void updateGuest(Guest guest) throws DAOException, NotFoundException, GuestInvalidException, GuestNameEmptyException, GuestEmailInvalidException, GuestEmailAlreadyExistsException, GuestPhoneInvalidException {
        logger.info("Attempting to update guest: guestId=" + guest.getId());
        validateGuest(guest);
        guestDAO.update(guest);
        logger.info("Guest updated successfully: guestId=" + guest.getId());
    }

    public void deleteGuest(int id) throws DAOException, NotFoundException {
        logger.info("Attempting to delete guest: guestId=" + id);
        guestDAO.delete(id);
        logger.info("Guest deleted successfully: guestId=" + id);
    }

    public String getGuestFirstName(int id) throws DAOException, NotFoundException {
        return guestDAO.getGuestFirstName(id);
    }

    public String getGuestLastName(int id) throws DAOException, NotFoundException {
        return guestDAO.getGuestLastName(id);
    }

    public String getGuestEmail(int id) throws DAOException, NotFoundException {
        return guestDAO.getGuestEmail(id);
    }

    public String getGuestPhone(int id) throws DAOException, NotFoundException {
        return guestDAO.getGuestPhone(id);
    }

    private void validateGuest(Guest guest) throws GuestInvalidException, GuestNameEmptyException, GuestEmailInvalidException, GuestEmailAlreadyExistsException, GuestPhoneInvalidException {
        if (guest == null) {
            logger.warning("Invalid guest: guest is null");
            throw new GuestInvalidException();
        }
        if (guest.getFirstName() == null || guest.getFirstName().isBlank()) {
            logger.warning("Invalid guest first name: firstName is null or empty");
            throw new GuestNameEmptyException(true, false);
        }
        if (guest.getLastName() == null || guest.getLastName().isBlank()) {
            logger.warning("Invalid guest last name: lastName is null or empty");
            throw new GuestNameEmptyException(false, true);
        }

        validateEmail(guest.getEmail());
        validatePhone(guest.getPhone());

        if (isEmailDuplicate(guest.getEmail(), guest.getId())) {
            logger.warning("Invalid guest email: email already exists");
            throw new GuestEmailAlreadyExistsException();
        }
    }

    private void validateEmail(String email) throws GuestEmailInvalidException {
        if (email == null || email.isBlank()) {
            logger.warning("Invalid guest email: email is null or empty");
            throw new GuestEmailInvalidException(true);
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            logger.warning("Invalid guest email: email format is not valid");
            throw new GuestEmailInvalidException(false);
        }
    }

    private boolean isEmailDuplicate(String email, Integer id) throws DAOException {
        return guestDAO.existsByEmail(email, id);
    }

    private void validatePhone(String phone) throws GuestPhoneInvalidException {
        if (phone == null || phone.isBlank()) {
            logger.warning("Invalid guest phone: phone is null or empty");
            throw new GuestPhoneInvalidException(true);
        }
        if (!phone.matches("^\\d{10}$")) {
            logger.warning("Invalid guest phone: phone number format is not valid");
            throw new GuestPhoneInvalidException(false);
        }
    }
}
