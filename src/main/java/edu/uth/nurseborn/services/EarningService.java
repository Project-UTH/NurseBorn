package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.EarningDTO;
import edu.uth.nurseborn.exception.UserNotFoundException;
import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.Earning;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.BookingRepository;
import edu.uth.nurseborn.repositories.EarningRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EarningService {

    private static final Logger logger = LoggerFactory.getLogger(EarningService.class);

    @Autowired
    private EarningRepository earningRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Tạo một bản ghi thu nhập mới.
     *
     * @param earningDTO DTO chứa thông tin thu nhập
     * @return EarningDTO đã được tạo
     */
    @Transactional
    public EarningDTO createEarning(EarningDTO earningDTO) {
        logger.debug("Tạo thu nhập mới cho bookingId: {}, nurseUserId: {}",
                earningDTO.getBookingId(), earningDTO.getNurseUserId());

        // Kiểm tra user (nurse) tồn tại
        Optional<User> userOptional = userRepository.findById(Long.valueOf(earningDTO.getNurseUserId()));
        if (!userOptional.isPresent()) {
            logger.warn("Không tìm thấy user với ID: {}", earningDTO.getNurseUserId());
            throw new UserNotFoundException("Không tìm thấy user với ID: " + earningDTO.getNurseUserId());
        }
        User nurse = userOptional.get();

        // Kiểm tra booking tồn tại
        Optional<Booking> bookingOptional = bookingRepository.findById(earningDTO.getBookingId());
        if (!bookingOptional.isPresent()) {
            logger.warn("Không tìm thấy booking với ID: {}", earningDTO.getBookingId());
            throw new RuntimeException("Không tìm thấy booking với ID: " + earningDTO.getBookingId());
        }
        Booking booking = bookingOptional.get();

        // Kiểm tra xem earning đã tồn tại cho booking này chưa
        Optional<Earning> existingEarning = earningRepository.findByBookingBookingId(earningDTO.getBookingId());
        if (existingEarning.isPresent()) {
            logger.warn("Thu nhập đã tồn tại cho bookingId: {}", earningDTO.getBookingId());
            throw new IllegalStateException("Thu nhập đã tồn tại cho booking này");
        }

        try {
            // Ánh xạ DTO sang entity
            Earning earning = new Earning();
            earning.setBooking(booking);
            earning.setNurse(nurse);
            earning.setAmount(earningDTO.getAmount());
            earning.setPlatformFee(earningDTO.getPlatformFee() != null ? earningDTO.getPlatformFee() : 0.0);

            logger.debug("Earning entity trước khi lưu: {}", earning);
            Earning savedEarning = earningRepository.save(earning);
            earningRepository.flush();
            logger.info("Đã lưu thu nhập thành công cho bookingId: {}", savedEarning.getBooking().getBookingId());

            // Ánh xạ entity trở lại DTO
            return modelMapper.map(savedEarning, EarningDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi khi lưu thu nhập: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi lưu thu nhập: " + ex.getMessage());
        }
    }

    /**
     * Lấy danh sách thu nhập của một y tá theo nurseUserId.
     *
     * @param nurseUserId ID của y tá
     * @return Danh sách EarningDTO
     */
    public List<EarningDTO> getEarningsByNurseUserId(Long nurseUserId) {
        logger.debug("Lấy danh sách thu nhập cho nurseUserId: {}", nurseUserId);

        // Kiểm tra user tồn tại
        Optional<User> userOptional = userRepository.findById(nurseUserId);
        if (!userOptional.isPresent()) {
            logger.warn("Không tìm thấy user với ID: {}", nurseUserId);
            throw new UserNotFoundException("Không tìm thấy user với ID: " + nurseUserId);
        }

        List<Earning> earnings = earningRepository.findByNurseUserId(nurseUserId);
        return earnings.stream()
                .map(earning -> modelMapper.map(earning, EarningDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin thu nhập theo earningId.
     *
     * @param earningId ID của thu nhập
     * @return EarningDTO
     */
    public EarningDTO getEarningById(Integer earningId) {
        logger.debug("Lấy thu nhập với earningId: {}", earningId);

        Optional<Earning> earningOptional = earningRepository.findById(earningId);
        if (!earningOptional.isPresent()) {
            logger.warn("Không tìm thấy thu nhập với ID: {}", earningId);
            throw new RuntimeException("Không tìm thấy thu nhập với ID: " + earningId);
        }

        return modelMapper.map(earningOptional.get(), EarningDTO.class);
    }

    /**
     * Cập nhật thông tin thu nhập.
     *
     * @param earningId ID của thu nhập
     * @param earningDTO DTO chứa thông tin cập nhật
     * @return EarningDTO đã được cập nhật
     */
    @Transactional
    public EarningDTO updateEarning(Integer earningId, EarningDTO earningDTO) {
        logger.debug("Cập nhật thu nhập với earningId: {}", earningId);

        Optional<Earning> earningOptional = earningRepository.findById(earningId);
        if (!earningOptional.isPresent()) {
            logger.warn("Không tìm thấy thu nhập với ID: {}", earningId);
            throw new RuntimeException("Không tìm thấy thu nhập với ID: " + earningId);
        }

        Earning earning = earningOptional.get();

        try {
            // Cập nhật các trường cần thiết
            if (earningDTO.getAmount() != null) {
                earning.setAmount(earningDTO.getAmount());
            }
            if (earningDTO.getPlatformFee() != null) {
                earning.setPlatformFee(earningDTO.getPlatformFee());
            }

            logger.debug("Earning entity trước khi cập nhật: {}", earning);
            Earning updatedEarning = earningRepository.save(earning);
            earningRepository.flush();
            logger.info("Đã cập nhật thu nhập thành công cho earningId: {}", earningId);

            return modelMapper.map(updatedEarning, EarningDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi khi cập nhật thu nhập: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi cập nhật thu nhập: " + ex.getMessage());
        }
    }

    /**
     * Xóa thu nhập theo earningId.
     *
     * @param earningId ID của thu nhập
     */
    @Transactional
    public void deleteEarning(Integer earningId) {
        logger.debug("Xóa thu nhập với earningId: {}", earningId);

        Optional<Earning> earningOptional = earningRepository.findById(earningId);
        if (!earningOptional.isPresent()) {
            logger.warn("Không tìm thấy thu nhập với ID: {}", earningId);
            throw new RuntimeException("Không tìm thấy thu nhập với ID: " + earningId);
        }

        try {
            earningRepository.delete(earningOptional.get());
            earningRepository.flush();
            logger.info("Đã xóa thu nhập thành công cho earningId: {}", earningId);
        } catch (Exception ex) {
            logger.error("Lỗi khi xóa thu nhập: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi xóa thu nhập: " + ex.getMessage());
        }
    }
}