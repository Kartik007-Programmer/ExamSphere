package com.examsphere.question.Service;

import com.examsphere.question.DTO.CreateQuestionRequest;
import com.examsphere.question.DTO.QuestionResponse;
import com.examsphere.question.DTO.StudentQuestionResponse;
import com.examsphere.question.Entity.Question;
import com.examsphere.question.Repository.QuestionRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository repository;

    public QuestionService(QuestionRepository repository) {
        this.repository = repository;
    }

    @CacheEvict(
            cacheNames = {"studentQuestions", "studentQuestionLists"},
            allEntries = true
    )
    @Transactional
    public QuestionResponse create(CreateQuestionRequest request,
                                   Long createdById) {
        Question question = new Question(
                request.questionText(),
                request.optionA(),
                request.optionB(),
                request.optionC(),
                request.optionD(),
                request.correctOption(),
                request.topic(),
                request.difficulty(),
                createdById
        );

        Question savedQuestion = repository.save(question);

        return toResponse(savedQuestion);
    }

    @CacheEvict(
            cacheNames = {"studentQuestions", "studentQuestionLists"},
            allEntries = true
    )
    @Transactional
    public List<QuestionResponse> createMultiple(List<CreateQuestionRequest> request,
                                                 Long createdById) {
        List<Question> question = request.stream()
                .map(r -> new Question(
                        r.questionText(),
                        r.optionA(),
                        r.optionB(),
                        r.optionC(),
                        r.optionD(),
                        r.correctOption(),
                        r.topic(),
                        r.difficulty(),
                        createdById
                )).toList();

        List<Question> savedQuestion = repository.saveAll(question);

        return savedQuestion.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public QuestionResponse getById(Long id) {
        return toResponse(findQuestion(id));
    }

    @Transactional(readOnly = true)
    public Page<QuestionResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    @CacheEvict(
            cacheNames = {"studentQuestions", "studentQuestionLists"},
            allEntries = true
    )
    @Transactional
    public QuestionResponse update(
            Long id,
            CreateQuestionRequest request,
            Long currentUserId,
            boolean isAdmin) {

        Question question = findQuestion(id);

        verifyOwnershipOrAdmin(question, currentUserId, isAdmin);

        question.update(
                request.questionText(),
                request.optionA(),
                request.optionB(),
                request.optionC(),
                request.optionD(),
                request.correctOption(),
                request.topic(),
                request.difficulty()
        );

        return toResponse(question);
    }

    @CacheEvict(
            cacheNames = {"studentQuestions", "studentQuestionLists"},
            allEntries = true
    )
    @Transactional
    public void delete(
            Long id,
            Long currentUserId,
            boolean isAdmin) {

        Question question = findQuestion(id);

        verifyOwnershipOrAdmin(question, currentUserId, isAdmin);

        repository.delete(question);
    }

    @Cacheable(
            cacheNames = "studentQuestions",
            key = "#id"
    )
    @Transactional(readOnly = true)
    public StudentQuestionResponse getStudentQuestionById(Long id) {

        Question question = findQuestion(id);

        return toStudentResponse(question);
    }

    private StudentQuestionResponse toStudentResponse(Question question) {
        return new StudentQuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getDifficulty()
        );
    }

    private Question findQuestion(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Question not found"));
    }

    private QuestionResponse toResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getCorrectOption(),
                question.getTopic(),
                question.getDifficulty(),
                question.getCreatedById(),
                question.getCreatedAt()
        );
    }

    private void verifyOwnershipOrAdmin(
            Question question,
            Long currentUserId,
            boolean isAdmin) {

        if (!isAdmin &&
                !question.getCreatedById().equals(currentUserId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot modify another teacher's question");
        }
    }
}