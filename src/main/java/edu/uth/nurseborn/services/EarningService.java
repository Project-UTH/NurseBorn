package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.EarningDTO;
import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.Earning;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.BookingRepository;
import edu.uth.nurseborn.repositories.EarningRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EarningService {

    private static final Logger logger = LoggerFactory.getLogger(EarningService.class);

    private final EarningRepository earningRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;

    // Constructor injection
    public EarningService(EarningRepository earningRepository, 
                         UserRepository userRepository, 
                         BookingRepository bookingRepository,
                         ModelMapper modelMapper) {
        this.earningRepository = earningRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public EarningDTO createEarning(EarningDTO earningDTO) {
        logger.debug("Bắt đầu tạo bản ghi thu nhập: {}", earningDTO);

        try {
            User nurse = userRepository.findById(earningDTO.getNurseUserId().longValue())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nurse với ID: " + earningDTO.getNurseUserId()));
            Booking booking = bookingRepository.findById(earningDTO.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + earningDTO.getBookingId()));

            Earning earning = new Earning();
            earning.setNurse(nurse);
            earning.setBooking(booking);
            earning.setAmount(earningDTO.getAmount());
            earning.setPlatformFee(earningDTO.getPlatformFee());
            // transactionDate sẽ được set tự động bởi @PrePersist

            logger.debug("Earning entity trước khi lưu: {}", earning);

            Earning savedEarning = earningRepository.save(earning);
            earningRepository.flush();
            logger.info("Đã tạo bản ghi thu nhập thành công với ID: {}", savedEarning.getEarningId());

            return mapToDTO(savedEarning);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo bản ghi thu nhập: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo bản ghi thu nhập: " + e.getMessage());
        }
    }

    public List<EarningDTO> getEarningsByNurseId(Integer nurseUserId) {
        logger.debug("Tìm thu nhập với nurseUserId: {}", nurseUserId);
        User nurse = userRepository.findById(nurseUserId.longValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nurse với ID: " + nurseUserId));
        List<Earning> earnings = earningRepository.findByNurse(nurse);
        return earnings.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public EarningDTO getEarningById(Integer earningId) {
        logger.debug("Tìm bản ghi thu nhập với earningId: {}", earningId);
        Earning earning = earningRepository.findById(earningId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi thu nhập với ID: " + earningId));
        return mapToDTO(earning);
    }

    @Transactional
    public void deleteEarning(Integer earningId) {
        logger.debug("Xóa bản ghi thu nhập với earningId: {}", earningId);
        Earning earning = earningRepository.findById(earningId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi thu nhập với ID: " + earningId));
        earningRepository.delete(earning);
        logger.info("Đã xóa bản ghi thu nhập với earningId: {}", earningId);
    }

    public List<EarningDTO> getEarningsByDateRange(String startDate, String endDate) {
        logger.debug("Tìm thu nhập từ {} đến {}", startDate, endDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);

        List<Earning> earnings = earningRepository.findByTransactionDateBetween(start, end);
        return earnings.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Double getTotalNetIncomeByNurseId(Integer nurseUserId) {
        logger.debug("Tính tổng net income cho nurseUserId: {}", nurseUserId);
        User nurse = userRepository.findById(nurseUserId.longValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nurse với ID: " + nurseUserId));
        Double total = earningRepository.sumNetIncomeByNurse(nurse);
        return total != null ? total : 0.0;
    }

    private EarningDTO mapToDTO(Earning earning) {
        EarningDTO dto = new EarningDTO();
        dto.setEarningId(earning.getEarningId());
        dto.setNurseUserId(earning.getNurse().getUserId().intValue());
        dto.setBookingId(earning.getBooking().getBookingId());
        dto.setAmount(earning.getAmount());
        dto.setPlatformFee(earning.getPlatformFee());
        dto.setNetIncome(earning.getNetIncome());
        dto.setTransactionDate(earning.getTransactionDate());
        return dto;
    }
}