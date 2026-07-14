import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import API from './api';

function Dashboard() {
    const navigate = useNavigate();

    // Authenticated user session details
    const [userRole, setUserRole] = useState(sessionStorage.getItem('role') || 'ROLE_STUDENT');
    const [userName, setUserName] = useState(sessionStorage.getItem('fullName') || 'User Workspace');
    const [userId, setUserId] = useState(sessionStorage.getItem('userId') || '1');

    const [assignments, setAssignments] = useState([]);
    const [submissions, setSubmissions] = useState([]); // Student's submissions list
    const [activeSubmissions, setActiveSubmissions] = useState([]); // Teacher's view of student submissions for selected assignment
    const [selectedAssignment, setSelectedAssignment] = useState(null);

    // Form inputs for Teacher Creating Assignment
    const [newTitle, setNewTitle] = useState('');
    const [newDescription, setNewDescription] = useState('');
    const [newSubject, setNewSubject] = useState('');
    const [newDeadline, setNewDeadline] = useState('');
    const [newFile, setNewFile] = useState(null);

    // Form inputs for Student Uploading Submission
    const [selectedUploadFile, setSelectedUploadFile] = useState({});

    // Form inputs for Teacher Grading
    const [gradeMarks, setGradeMarks] = useState('');
    const [gradeRemarks, setGradeRemarks] = useState('');
    const [activeGradingSubId, setActiveGradingSubId] = useState(null);

    // Status notifications/toasts
    const [message, setMessage] = useState('');
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        const token = sessionStorage.getItem('token');
        if (!token) {
            navigate('/');
            return;
        }
        fetchData();
    }, [userRole]);

    const fetchData = async () => {
        try {
            setErrorMsg('');
            // Fetch all assignments
            const assignRes = await API.get('/assignments/all');
            setAssignments(assignRes.data);

            // Fetch submissions if student
            if (userRole === 'ROLE_STUDENT') {
                const subRes = await API.get(`/submissions/student/${userId}`);
                setSubmissions(subRes.data);
            }
        } catch (err) {
            console.error(err);
            setErrorMsg('Failed to sync workspace data with server.');
        }
    };

    const showToast = (msg, isError = false) => {
        if (isError) {
            setErrorMsg(msg);
            setTimeout(() => setErrorMsg(''), 5000);
        } else {
            setMessage(msg);
            setTimeout(() => setMessage(''), 5000);
        }
    };

    // Teacher CRUD: Create Assignment
    const handleCreateAssignment = async (e) => {
        e.preventDefault();
        if (!newTitle || !newDeadline || !newSubject) return;

        try {
            const formData = new FormData();
            formData.append('title', newTitle);
            formData.append('description', newDescription || 'No description provided.');
            formData.append('subject', newSubject);
            // Format deadline to local date time ISO format: YYYY-MM-DDTHH:MM:SS
            const formattedDate = newDeadline.includes('T') ? newDeadline : `${newDeadline}T23:59:59`;
            formData.append('dueDate', formattedDate);
            formData.append('teacherId', userId);
            if (newFile) {
                formData.append('file', newFile);
            }

            const response = await API.post('/assignments/create', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });

            showToast(response.data.message || 'Assignment workspace created successfully!');
            setNewTitle('');
            setNewDescription('');
            setNewSubject('');
            setNewDeadline('');
            setNewFile(null);
            // Reset file input element visually
            document.getElementById('teacher-file-input').value = '';
            fetchData();
        } catch (err) {
            console.error(err);
            showToast(err.response?.data || 'Failed to publish assignment!', true);
        }
    };

    // Teacher CRUD: Delete Assignment
    const handleDeleteAssignment = async (id) => {
        if (!window.confirm('Are you sure you want to delete this assignment permanently?')) return;
        try {
            const response = await API.delete(`/assignments/${id}`);
            showToast(response.data.message || 'Assignment removed successfully!');
            if (selectedAssignment?.id === id) {
                setSelectedAssignment(null);
                setActiveSubmissions([]);
            }
            fetchData();
        } catch (err) {
            console.error(err);
            showToast('Failed to delete assignment from server.', true);
        }
    };

    // Student: Handle PDF Upload Action
    const handleFileUpload = async (assignmentId) => {
        const file = selectedUploadFile[assignmentId];
        if (!file) {
            alert('Please select a PDF document first.');
            return;
        }

        try {
            const formData = new FormData();
            formData.append('assignmentId', assignmentId);
            formData.append('studentId', userId);
            formData.append('file', file);

            const response = await API.post('/submissions/upload', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });

            showToast(response.data.message || 'Submission uploaded successfully!');
            fetchData();
        } catch (err) {
            console.error(err);
            showToast(err.response?.data?.error || 'Failed to upload PDF submission.', true);
        }
    };

    // Teacher: View Submissions for an Assignment
    const handleViewSubmissions = async (assignment) => {
        setSelectedAssignment(assignment);
        try {
            const response = await API.get(`/submissions/assignment/${assignment.id}`);
            setActiveSubmissions(response.data);
        } catch (err) {
            console.error(err);
            showToast('Failed to fetch student submissions for this assignment.', true);
        }
    };

    // Teacher: Grade a Submission
    const handleGradeSubmission = async (e) => {
        e.preventDefault();
        if (!gradeMarks || activeGradingSubId === null) return;

        try {
            const response = await API.put(`/submissions/grade/${activeGradingSubId}`, {
                marks: gradeMarks,
                remarks: gradeRemarks
            });

            showToast(response.data.message || 'Submission graded successfully!');
            setGradeMarks('');
            setGradeRemarks('');
            setActiveGradingSubId(null);
            // Refresh submissions list for the selected assignment
            if (selectedAssignment) {
                handleViewSubmissions(selectedAssignment);
            }
            fetchData();
        } catch (err) {
            console.error(err);
            showToast('Failed to update submission grade.', true);
        }
    };

    // Toggle Role for Development Demonstrations
    const handleRoleSwitch = () => {
        const nextRole = userRole === 'ROLE_STUDENT' ? 'ROLE_TEACHER' : 'ROLE_STUDENT';
        const nextUserId = nextRole === 'ROLE_TEACHER' ? '1' : '2';
        const nextName = nextRole === 'ROLE_TEACHER' ? 'Dr. Shernika S.S.' : 'John Doe';

        setUserRole(nextRole);
        setUserId(nextUserId);
        setUserName(nextName);

        sessionStorage.setItem('role', nextRole);
        sessionStorage.setItem('userId', nextUserId);
        sessionStorage.setItem('fullName', nextName);
        setSelectedAssignment(null);
        setActiveSubmissions([]);
    };

    const handleLogout = () => {
        sessionStorage.clear();
        navigate('/');
    };

    const getFileUrl = (fileName) => {
        return `http://localhost:8080/api/submissions/view/${fileName}`;
    };

    return (
        <div style={styles.container}>
            {/* Status Notifications */}
            {message && <div style={styles.successToast}>✓ {message}</div>}
            {errorMsg && <div style={styles.errorToast}>⚠️ {errorMsg}</div>}

            <div style={styles.dashboardCard}>
                {/* Header Profile Dashboard */}
                <div style={styles.header}>
                    <div>
                        <h1 style={styles.brandTitle}>🎓 SXCCE</h1>
                        <p style={styles.subtitle}>St. Xavier's Catholic College of Engineering</p>
                    </div>
                    <div style={styles.profileBox}>
                        <div style={{ textAlign: 'right', marginRight: '15px' }}>
                            <div style={styles.userName}>{userName}</div>
                            <span style={userRole === 'ROLE_TEACHER' ? styles.badgeTeacher : styles.badgeStudent}>
                                {userRole === 'ROLE_TEACHER' ? '👩‍🏫 STAFF / TEACHER' : '👨‍🎓 STUDENT'}
                            </span>
                        </div>
                        <div style={styles.headerActions}>
                            <button onClick={handleRoleSwitch} style={styles.switchBtn}>
                                Switch Role
                            </button>
                            <button onClick={handleLogout} style={styles.logoutBtn}>
                                Logout
                            </button>
                        </div>
                    </div>
                </div>

                {/* =================================================================== */}
                {/* STAFF / TEACHER WORKSPACE */}
                {/* =================================================================== */}
                {userRole === 'ROLE_TEACHER' && (
                    <div style={styles.portalGrid}>
                        {/* Control Panel: Add Assignment */}
                        <div style={styles.panelCard}>
                            <h3 style={styles.panelTitle}>🛠️ Publish New Assignment</h3>
                            <form onSubmit={handleCreateAssignment} style={styles.form}>
                                <div style={styles.inputGroup}>
                                    <label style={styles.label}>Assignment Title</label>
                                    <input type="text" placeholder="e.g. Java Spring Boot Core Concepts" value={newTitle} onChange={(e) => setNewTitle(e.target.value)} style={styles.input} required />
                                </div>

                                <div style={styles.inputRow}>
                                    <div style={{ flex: 1 }}>
                                        <label style={styles.label}>Subject Category</label>
                                        <input type="text" placeholder="e.g. Advanced Java Programming" value={newSubject} onChange={(e) => setNewSubject(e.target.value)} style={styles.input} required />
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <label style={styles.label}>Due Date & Time</label>
                                        <input type="datetime-local" value={newDeadline} onChange={(e) => setNewDeadline(e.target.value)} style={styles.input} required />
                                    </div>
                                </div>

                                <div style={styles.inputGroup}>
                                    <label style={styles.label}>Description / Guidelines</label>
                                    <textarea rows="3" placeholder="Provide detailed assignment guidelines here..." value={newDescription} onChange={(e) => setNewDescription(e.target.value)} style={styles.textarea} />
                                </div>

                                <div style={styles.inputGroup}>
                                    <label style={styles.label}>Reference PDF Attachment (Optional)</label>
                                    <input id="teacher-file-input" type="file" accept=".pdf" onChange={(e) => setNewFile(e.target.files[0])} style={styles.fileInput} />
                                </div>

                                <button type="submit" style={styles.submitBtn}>
                                    ➕ Create & Publish Assignment
                                </button>
                            </form>
                        </div>

                        {/* List & Submissions View */}
                        <div style={styles.panelCard}>
                            <h3 style={styles.panelTitle}>📋 Active Class Assignments</h3>
                            {assignments.length === 0 ? (
                                <p style={styles.emptyText}>No assignments published yet.</p>
                            ) : (
                                <div style={styles.assignmentList}>
                                    {assignments.map((item) => (
                                        <div key={item.id} style={selectedAssignment?.id === item.id ? styles.activeAssignCard : styles.assignCard}>
                                            <div style={styles.assignHeader}>
                                                <h4 style={styles.assignTitle}>{item.title}</h4>
                                                <span style={styles.subjectBadge}>{item.subject}</span>
                                            </div>
                                            <p style={styles.assignDesc}>{item.description}</p>
                                            <div style={styles.assignFooter}>
                                                <span style={styles.dueDateText}>📅 Due: {item.dueDate ? new Date(item.dueDate).toLocaleString() : 'N/A'}</span>
                                                {item.referenceFilePath && (
                                                    <a href={getFileUrl(item.referenceFilePath)} target="_blank" rel="noopener noreferrer" style={styles.refLink}>
                                                        📄 View Reference
                                                    </a>
                                                )}
                                            </div>
                                            <div style={styles.teacherActions}>
                                                <button onClick={() => handleViewSubmissions(item)} style={styles.viewSubBtn}>
                                                    🔍 View Submissions
                                                </button>
                                                <button onClick={() => handleDeleteAssignment(item.id)} style={styles.deleteBtn}>
                                                    🗑️ Delete
                                                </button>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}

                            {/* Submissions Section */}
                            {selectedAssignment && (
                                <div style={styles.submissionsSection}>
                                    <h3 style={styles.subSectionTitle}>
                                        📥 Submissions for: <span style={{ color: '#2563eb' }}>{selectedAssignment.title}</span>
                                    </h3>
                                    {activeSubmissions.length === 0 ? (
                                        <p style={styles.emptyText}>No student submissions received yet.</p>
                                    ) : (
                                        <div style={styles.subTableContainer}>
                                            <table style={styles.table}>
                                                <thead>
                                                    <tr style={styles.tableHeaderRow}>
                                                        <th style={styles.tableHeader}>Student</th>
                                                        <th style={styles.tableHeader}>Status</th>
                                                        <th style={styles.tableHeader}>Grade / Marks</th>
                                                        <th style={styles.tableHeader}>Document</th>
                                                        <th style={styles.tableHeader}>Actions</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {activeSubmissions.map((sub) => (
                                                        <tr key={sub.id} style={styles.tableRow}>
                                                            <td style={styles.tableCell}>
                                                                <div><b>{sub.student.fullName}</b></div>
                                                                <span style={{ fontSize: '11px', color: '#64748b' }}>{sub.student.identificationNumber}</span>
                                                            </td>
                                                            <td style={styles.tableCell}>
                                                                <span style={sub.status === 'EVALUATED' ? styles.statusEvaluated : styles.statusSubmitted}>
                                                                    {sub.status}
                                                                </span>
                                                            </td>
                                                            <td style={styles.tableCell}>
                                                                {sub.marksAwarded !== null ? (
                                                                    <b>{sub.marksAwarded} / 100</b>
                                                                ) : (
                                                                    <span style={{ color: '#ef4444' }}>Ungraded</span>
                                                                )}
                                                            </td>
                                                            <td style={styles.tableCell}>
                                                                <a href={getFileUrl(sub.submissionFilePath)} target="_blank" rel="noopener noreferrer" style={styles.fileLinkBtn}>
                                                                    📄 Open PDF
                                                                </a>
                                                            </td>
                                                            <td style={styles.tableCell}>
                                                                <button onClick={() => {
                                                                    setActiveGradingSubId(sub.id);
                                                                    setGradeMarks(sub.marksAwarded || '');
                                                                    setGradeRemarks(sub.teacherRemarks || '');
                                                                }} style={styles.gradeBtn}>
                                                                    📝 Grade
                                                                </button>
                                                            </td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    )}

                                    {/* Grading Modal Inline */}
                                    {activeGradingSubId && (
                                        <div style={styles.gradingFormCard}>
                                            <h4 style={{ margin: '0 0 15px 0', color: '#1e293b' }}>📝 Submit Evaluation Score</h4>
                                            <form onSubmit={handleGradeSubmission} style={styles.gradeFormInline}>
                                                <div style={{ flex: 1 }}>
                                                    <label style={styles.label}>Score / Marks (0-100)</label>
                                                    <input type="number" min="0" max="100" value={gradeMarks} onChange={(e) => setGradeMarks(e.target.value)} style={styles.input} required />
                                                </div>
                                                <div style={{ flex: 2 }}>
                                                    <label style={styles.label}>Remarks / Feedback</label>
                                                    <input type="text" placeholder="Good effort, keep it up!" value={gradeRemarks} onChange={(e) => setGradeRemarks(e.target.value)} style={styles.input} />
                                                </div>
                                                <div style={styles.gradeActionsInline}>
                                                    <button type="submit" style={styles.saveGradeBtn}>Save</button>
                                                    <button type="button" onClick={() => setActiveGradingSubId(null)} style={styles.cancelGradeBtn}>Cancel</button>
                                                </div>
                                            </form>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {/* =================================================================== */}
                {/* STUDENT PORTAL */}
                {/* =================================================================== */}
                {userRole === 'ROLE_STUDENT' && (
                    <div>
                        <h3 style={styles.panelTitle}>📖 Available Assignments & Submission Hub</h3>
                        {assignments.length === 0 ? (
                            <p style={styles.emptyText}>No assignments are currently published by the faculty.</p>
                        ) : (
                            <div style={styles.studentGrid}>
                                {assignments.map((item) => {
                                    // Find student's submission for this assignment
                                    const userSub = submissions.find(sub => sub.assignment.id === item.id);

                                    return (
                                        <div key={item.id} style={styles.studentCard}>
                                            <div style={styles.cardHeaderRow}>
                                                <h4 style={styles.assignTitle}>{item.title}</h4>
                                                <span style={styles.subjectBadge}>{item.subject}</span>
                                            </div>
                                            <p style={styles.assignDesc}>{item.description}</p>

                                            <div style={styles.metaInfo}>
                                                <div>📅 Due Date: <b>{item.dueDate ? new Date(item.dueDate).toLocaleString() : 'N/A'}</b></div>
                                                {item.referenceFilePath && (
                                                    <div style={{ marginTop: '5px' }}>
                                                        📎 Reference attachment: {' '}
                                                        <a href={getFileUrl(item.referenceFilePath)} target="_blank" rel="noopener noreferrer" style={styles.downloadLink}>
                                                            Download PDF Guidelines
                                                        </a>
                                                    </div>
                                                )}
                                            </div>

                                            <div style={styles.submissionStatusBox}>
                                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                    <span>Submission Status:</span>
                                                    <span style={userSub ? (userSub.status === 'EVALUATED' ? styles.statusEvaluated : styles.statusSubmitted) : styles.statusPending}>
                                                        {userSub ? userSub.status : 'PENDING'}
                                                    </span>
                                                </div>

                                                {/* Gradings */}
                                                {userSub && userSub.status === 'EVALUATED' && (
                                                    <div style={styles.gradesContainer}>
                                                        <div style={{ margin: '5px 0' }}>Marks Awarded: <b style={{ color: '#10b981', fontSize: '16px' }}>{userSub.marksAwarded} / 100</b></div>
                                                        <div style={{ fontStyle: 'italic', fontSize: '13px', color: '#475569' }}>Remarks: "{userSub.teacherRemarks}"</div>
                                                    </div>
                                                )}

                                                {/* Upload form if not submitted */}
                                                {!userSub && (
                                                    <div style={styles.uploadForm}>
                                                        <input type="file" accept=".pdf" onChange={(e) => {
                                                            setSelectedUploadFile({
                                                                ...selectedUploadFile,
                                                                [item.id]: e.target.files[0]
                                                            });
                                                        }} style={styles.studentFileInput} />
                                                        <button onClick={() => handleFileUpload(item.id)} style={styles.uploadBtn}>
                                                            📤 Submit PDF Work
                                                        </button>
                                                    </div>
                                                )}

                                                {userSub && (
                                                    <div style={{ marginTop: '10px', textAlign: 'right' }}>
                                                        <a href={getFileUrl(userSub.submissionFilePath)} target="_blank" rel="noopener noreferrer" style={styles.viewSubPdfLink}>
                                                            📄 View My Submitted PDF
                                                        </a>
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

// Styling Object representing clean premium layout
const styles = {
    container: {
        padding: '30px',
        fontFamily: "'Outfit', 'Inter', sans-serif",
        background: 'linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)',
        minHeight: '100vh',
    },
    dashboardCard: {
        maxWidth: '1200px',
        margin: '0 auto',
        background: '#ffffff',
        padding: '35px',
        borderRadius: '16px',
        boxShadow: '0 10px 25px rgba(15, 23, 42, 0.08)',
    },
    header: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        borderBottom: '2px solid #f1f5f9',
        paddingBottom: '25px',
        marginBottom: '30px',
    },
    brandTitle: {
        fontSize: '28px',
        color: '#0f172a',
        margin: 0,
        fontWeight: 800,
    },
    subtitle: {
        margin: '5px 0 0 0',
        color: '#64748b',
        fontSize: '14px',
    },
    profileBox: {
        display: 'flex',
        alignItems: 'center',
    },
    userName: {
        fontWeight: 'bold',
        fontSize: '16px',
        color: '#1e293b',
    },
    badgeTeacher: {
        background: 'linear-gradient(135deg, #7c3aed, #4f46e5)',
        color: '#ffffff',
        padding: '4px 10px',
        borderRadius: '20px',
        fontSize: '11px',
        fontWeight: 'bold',
        display: 'inline-block',
        marginTop: '4px',
    },
    badgeStudent: {
        background: 'linear-gradient(135deg, #2563eb, #3b82f6)',
        color: '#ffffff',
        padding: '4px 10px',
        borderRadius: '20px',
        fontSize: '11px',
        fontWeight: 'bold',
        display: 'inline-block',
        marginTop: '4px',
    },
    headerActions: {
        display: 'flex',
        gap: '10px',
    },
    switchBtn: {
        padding: '8px 14px',
        background: '#f1f5f9',
        color: '#475569',
        border: '1px solid #cbd5e1',
        borderRadius: '8px',
        cursor: 'pointer',
        fontWeight: '600',
        transition: 'all 0.2s',
    },
    logoutBtn: {
        padding: '8px 16px',
        background: '#ef4444',
        color: '#ffffff',
        border: 'none',
        borderRadius: '8px',
        cursor: 'pointer',
        fontWeight: 'bold',
        transition: 'all 0.2s',
    },
    portalGrid: {
        display: 'grid',
        gridTemplateColumns: '1fr 1.3fr',
        gap: '30px',
    },
    panelCard: {
        background: '#f8fafc',
        borderRadius: '12px',
        padding: '24px',
        border: '1px solid #e2e8f0',
    },
    panelTitle: {
        margin: '0 0 20px 0',
        color: '#0f172a',
        fontSize: '18px',
        fontWeight: '700',
        borderBottom: '1px solid #cbd5e1',
        paddingBottom: '10px',
    },
    form: {
        display: 'flex',
        flexDirection: 'column',
        gap: '15px',
    },
    inputGroup: {
        display: 'flex',
        flexDirection: 'column',
        gap: '5px',
    },
    inputRow: {
        display: 'flex',
        gap: '15px',
    },
    label: {
        fontSize: '13px',
        fontWeight: '600',
        color: '#475569',
    },
    input: {
        width: '100%',
        padding: '10px',
        borderRadius: '8px',
        border: '1px solid #cbd5e1',
        background: '#ffffff',
        boxSizing: 'border-box',
        fontSize: '14px',
    },
    textarea: {
        width: '100%',
        padding: '10px',
        borderRadius: '8px',
        border: '1px solid #cbd5e1',
        background: '#ffffff',
        boxSizing: 'border-box',
        fontSize: '14px',
        fontFamily: 'inherit',
    },
    fileInput: {
        fontSize: '13px',
    },
    submitBtn: {
        padding: '12px',
        background: '#10b981',
        color: '#ffffff',
        border: 'none',
        borderRadius: '8px',
        fontWeight: 'bold',
        fontSize: '14px',
        cursor: 'pointer',
        transition: 'background 0.2s',
        marginTop: '10px',
    },
    emptyText: {
        color: '#64748b',
        fontStyle: 'italic',
        fontSize: '14px',
    },
    assignmentList: {
        display: 'flex',
        flexDirection: 'column',
        gap: '15px',
        maxHeight: '400px',
        overflowY: 'auto',
        marginBottom: '20px',
    },
    assignCard: {
        background: '#ffffff',
        border: '1px solid #e2e8f0',
        borderRadius: '10px',
        padding: '15px',
        transition: 'transform 0.2s, box-shadow 0.2s',
    },
    activeAssignCard: {
        background: '#ffffff',
        border: '2px solid #3b82f6',
        borderRadius: '10px',
        padding: '15px',
        boxShadow: '0 4px 12px rgba(59, 130, 246, 0.1)',
    },
    assignHeader: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'flex-start',
    },
    assignTitle: {
        margin: 0,
        fontSize: '16px',
        fontWeight: '700',
        color: '#1e293b',
    },
    subjectBadge: {
        fontSize: '10px',
        background: '#eff6ff',
        color: '#2563eb',
        padding: '3px 8px',
        borderRadius: '12px',
        fontWeight: 'bold',
    },
    assignDesc: {
        fontSize: '13px',
        color: '#475569',
        margin: '10px 0',
        lineHeight: 1.4,
    },
    assignFooter: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        fontSize: '12px',
    },
    dueDateText: {
        color: '#94a3b8',
    },
    refLink: {
        color: '#2563eb',
        fontWeight: 'bold',
        textDecoration: 'none',
    },
    teacherActions: {
        marginTop: '12px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    viewSubBtn: {
        padding: '6px 12px',
        background: '#3b82f6',
        color: 'white',
        border: 'none',
        borderRadius: '6px',
        cursor: 'pointer',
        fontSize: '12px',
        fontWeight: 'bold',
    },
    deleteBtn: {
        padding: '6px 12px',
        background: '#ef4444',
        color: 'white',
        border: 'none',
        borderRadius: '6px',
        cursor: 'pointer',
        fontSize: '12px',
        fontWeight: 'bold',
    },
    submissionsSection: {
        marginTop: '25px',
        borderTop: '2px solid #cbd5e1',
        paddingTop: '20px',
    },
    subSectionTitle: {
        fontSize: '16px',
        fontWeight: '700',
        color: '#1e293b',
        margin: '0 0 15px 0',
    },
    subTableContainer: {
        background: '#ffffff',
        border: '1px solid #e2e8f0',
        borderRadius: '8px',
        overflow: 'hidden',
    },
    table: {
        width: '100%',
        borderCollapse: 'collapse',
        fontSize: '13px',
    },
    tableHeaderRow: {
        background: '#f1f5f9',
        textAlign: 'left',
    },
    tableHeader: {
        padding: '12px 16px',
        color: '#475569',
        fontWeight: 'bold',
    },
    tableRow: {
        borderBottom: '1px solid #f1f5f9',
    },
    tableCell: {
        padding: '12px 16px',
        color: '#334155',
    },
    statusEvaluated: {
        background: '#d1fae5',
        color: '#065f46',
        padding: '2px 8px',
        borderRadius: '12px',
        fontSize: '10px',
        fontWeight: 'bold',
    },
    statusSubmitted: {
        background: '#dbeafe',
        color: '#1e40af',
        padding: '2px 8px',
        borderRadius: '12px',
        fontSize: '10px',
        fontWeight: 'bold',
    },
    statusPending: {
        background: '#fef3c7',
        color: '#92400e',
        padding: '2px 8px',
        borderRadius: '12px',
        fontSize: '10px',
        fontWeight: 'bold',
    },
    fileLinkBtn: {
        color: '#3b82f6',
        textDecoration: 'none',
        fontWeight: 'bold',
    },
    gradeBtn: {
        padding: '4px 8px',
        background: '#8b5cf6',
        color: 'white',
        border: 'none',
        borderRadius: '4px',
        cursor: 'pointer',
        fontSize: '11px',
        fontWeight: 'bold',
    },
    gradingFormCard: {
        marginTop: '15px',
        background: '#f5f3ff',
        border: '1px solid #ddd6fe',
        borderRadius: '8px',
        padding: '15px',
    },
    gradeFormInline: {
        display: 'flex',
        gap: '15px',
        alignItems: 'flex-end',
    },
    gradeActionsInline: {
        display: 'flex',
        gap: '5px',
    },
    saveGradeBtn: {
        padding: '10px 15px',
        background: '#10b981',
        color: 'white',
        border: 'none',
        borderRadius: '6px',
        cursor: 'pointer',
        fontWeight: 'bold',
    },
    cancelGradeBtn: {
        padding: '10px 15px',
        background: '#64748b',
        color: 'white',
        border: 'none',
        borderRadius: '6px',
        cursor: 'pointer',
        fontWeight: 'bold',
    },
    studentGrid: {
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gap: '20px',
    },
    studentCard: {
        background: '#f8fafc',
        border: '1px solid #e2e8f0',
        borderRadius: '12px',
        padding: '20px',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
    },
    cardHeaderRow: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'flex-start',
    },
    metaInfo: {
        fontSize: '13px',
        color: '#475569',
        background: '#ffffff',
        border: '1px solid #f1f5f9',
        padding: '10px',
        borderRadius: '8px',
        margin: '15px 0',
    },
    downloadLink: {
        color: '#3b82f6',
        fontWeight: 'bold',
        textDecoration: 'none',
    },
    submissionStatusBox: {
        background: '#ffffff',
        border: '1px solid #e2e8f0',
        borderRadius: '8px',
        padding: '12px',
        fontSize: '13px',
    },
    gradesContainer: {
        marginTop: '10px',
        paddingTop: '10px',
        borderTop: '1px dashed #e2e8f0',
    },
    uploadForm: {
        marginTop: '12px',
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
    },
    studentFileInput: {
        fontSize: '12px',
    },
    uploadBtn: {
        padding: '8px 12px',
        background: '#2563eb',
        color: 'white',
        border: 'none',
        borderRadius: '6px',
        cursor: 'pointer',
        fontWeight: 'bold',
        fontSize: '13px',
    },
    viewSubPdfLink: {
        color: '#10b981',
        fontWeight: 'bold',
        textDecoration: 'none',
        fontSize: '12px',
    },
    successToast: {
        position: 'fixed',
        top: '20px',
        right: '20px',
        background: '#10b981',
        color: 'white',
        padding: '12px 24px',
        borderRadius: '8px',
        boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
        zIndex: 1000,
        fontWeight: 'bold',
    },
    errorToast: {
        position: 'fixed',
        top: '20px',
        right: '20px',
        background: '#ef4444',
        color: 'white',
        padding: '12px 24px',
        borderRadius: '8px',
        boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
        zIndex: 1000,
        fontWeight: 'bold',
    }
};

export default Dashboard;