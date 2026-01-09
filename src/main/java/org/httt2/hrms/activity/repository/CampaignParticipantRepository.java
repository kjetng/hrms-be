package org.httt2.hrms.activity.repository;

import org.httt2.hrms.activity.entity.CampaignParticipant;
import org.httt2.hrms.activity.entity.id.CampaignParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignParticipantRepository extends JpaRepository<CampaignParticipant, CampaignParticipantId> {
    
    // Kiểm tra xem nhân viên (theo empId) đã tham gia chiến dịch này chưa
    // Spring Data JPA sẽ tự parse: Id -> EmpId và Id -> CampaignId
    boolean existsByEmpIdAndCampaignId(Long empId, Long campaignId);
    
    // Đếm số lượng người tham gia
    long countByCampaignId(Long campaignId);
    
    // Chỉ lấy những người đang JOINED (không lấy người đã LEFT)
    List<CampaignParticipant> findByEmpIdAndStatus(Long empId, org.httt2.hrms.activity.entity.ParticipantStatus status);

    // Tìm record cụ thể (để update status khi rời)
    Optional<CampaignParticipant> findByEmpIdAndCampaignId(Long empId, Long campaignId);
}