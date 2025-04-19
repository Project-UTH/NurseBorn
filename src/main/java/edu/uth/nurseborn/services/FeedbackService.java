package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.FeedbackDTO;
import edu.uth.nurseborn.exception.FeedbackNotFoundException;
import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.Feedback;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.BookingRepository;
import edu.uth.nurseborn.repositories.FeedbackRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public FeedbackDTO createFeedback(FeedbackDTO danhGiaDTO) {
        logger.debug("Bắt đầu transaction để tạo đánh giá: {}", danhGiaDTO);

        Booking booking = bookingRepository.findById(Long.valueOf(danhGiaDTO.getBookingId()))
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy lịch đặt với ID: {}", danhGiaDTO.getBookingId());
                    return new FeedbackNotFoundException("Lịch đặt không tồn tại với ID: " + danhGiaDTO.getBookingId());
                });

        User family = userRepository.findById(danhGiaDTO.getFamilyUserId())
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy gia đình với ID: {}", danhGiaDTO.getFamilyUserId());
                    return new FeedbackNotFoundException("Gia đình không tồn tại với ID: " + danhGiaDTO.getFamilyUserId());
                });

        User nurse = userRepository.findById(danhGiaDTO.getNurseUserId())
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy y tá với ID: {}", danhGiaDTO.getNurseUserId());
                    return new FeedbackNotFoundException("Y tá không tồn tại với ID: " + danhGiaDTO.getNurseUserId());
                });

        if (danhGiaDTO.getRating() < 1 || danhGiaDTO.getRating() > 5) {
            logger.warn("Điểm đánh giá không hợp lệ: {}", danhGiaDTO.getRating());
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5");
        }

        try {
            Feedback danhGia = new Feedback();
            danhGia.setBooking(booking);
            danhGia.setFamily(family);
            danhGia.setNurse(nurse);
            danhGia.setRating(danhGiaDTO.getRating());
            danhGia.setComment(danhGiaDTO.getComment());
            danhGia.setResponse(danhGiaDTO.getResponse());

            logger.debug("Đánh giá entity trước khi lưu: {}", danhGia);
            Feedback danhGiaDaLuu = feedbackRepository.save(danhGia);
            feedbackRepository.flush();
            logger.info("Đã lưu đánh giá thành công với ID: {}", danhGiaDaLuu.getFeedbackId());

            return modelMapper.map(danhGiaDaLuu, FeedbackDTO.class);
        } catch (Exception e) {
            logger.error("Lỗi khi lưu đánh giá: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tạo đánh giá: " + e.getMessage());
        }
    }

    public FeedbackDTO getFeedbackByBooking(Integer bookingId) {
        logger.debug("Tìm đánh giá cho lịch đặt ID: {}", bookingId);
        Booking booking = bookingRepository.findById(Long.valueOf(bookingId))
                .orElseThrow(() -> new FeedbackNotFoundException("Lịch đặt không tồn tại với ID: " + bookingId));

        return feedbackRepository.findOneByBooking(booking)
                .map(danhGia -> modelMapper.map(danhGia, FeedbackDTO.class))
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy đánh giá cho lịch đặt ID: {}", bookingId);
                    return new FeedbackNotFoundException("Không tìm thấy đánh giá cho lịch đặt ID: " + bookingId);
                });
    }

    public FeedbackDTO getFeedbackByBookingAndNurse(Integer bookingId, Long nurseUserId) {
        logger.debug("Tìm đánh giá cho lịch đặt ID: {} và y tá ID: {}", bookingId, nurseUserId);
        Booking booking = bookingRepository.findById(Long.valueOf(bookingId))
                .orElseThrow(() -> new FeedbackNotFoundException("Lịch đặt không tồn tại với ID: " + bookingId));
        User nurse = userRepository.findById(nurseUserId)
                .orElseThrow(() -> new FeedbackNotFoundException("Y tá không tồn tại với ID: " + nurseUserId));

        return feedbackRepository.findOneByBookingAndNurse(booking, nurse)
                .map(danhGia -> modelMapper.map(danhGia, FeedbackDTO.class))
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy đánh giá cho lịch đặt ID: {} và y tá ID: {}", bookingId, nurseUserId);
                    return new FeedbackNotFoundException("Không tìm thấy đánh giá cho lịch đặt ID: " + bookingId + " và y tá ID: " + nurseUserId);
                });
    }

    public List<FeedbackDTO> getFeedbacksByNurse(Long nurseUserId) {
        logger.debug("Tìm tất cả đánh giá cho y tá ID: {}", nurseUserId);
        List<Feedback> danhGiaList = feedbackRepository.findByNurseUserId(nurseUserId);
        return danhGiaList.stream()
                .map(danhGia -> modelMapper.map(danhGia, FeedbackDTO.class))
                .collect(Collectors.toList());
    }

    public List<FeedbackDTO> getFeedbacksByFamily(Long familyUserId) {
        logger.debug("Tìm tất cả đánh giá cho gia đình ID: {}", familyUserId);
        List<Feedback> danhGiaList = feedbackRepository.findByFamilyUserId(familyUserId);
        return danhGiaList.stream()
                .map(danhGia -> modelMapper.map(danhGia, FeedbackDTO.class))
                .collect(Collectors.toList());
    }

    public FeedbackDTO getFeedbackById(Integer feedbackId) {
        logger.debug("Tìm đánh giá với ID: {}", feedbackId);
        return feedbackRepository.findByFeedbackId(feedbackId)
                .map(danhGia -> modelMapper.map(danhGia, FeedbackDTO.class))
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy đánh giá với ID: {}", feedbackId);
                    return new FeedbackNotFoundException("Không tìm thấy đánh giá với ID: " + feedbackId);
                });
    }

    public List<Feedback> getFeedbacksByUser(User user) {
        if ("FAMILY".equalsIgnoreCase(user.getRole().name())) {
            return feedbackRepository.findByFamily(user);
        } else if ("NURSE".equalsIgnoreCase(user.getRole().name())) {
            return feedbackRepository.findByNurse(user);
        } else {
            throw new IllegalArgumentException("User role must be FAMILY or NURSE");
        }
    }
}