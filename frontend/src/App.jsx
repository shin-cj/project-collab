import { useEffect, useState } from 'react'

function App() {
  const [projectId, setProjectId] = useState('1')
  const [requesterId, setRequesterId] = useState('1')
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState('')
  const [tasks, setTasks] = useState([])
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [assigneeId, setAssigneeId] = useState('3')
  const [userName, setUserName] = useState('')
  const [userEmail, setUserEmail] = useState('')
  const [message, setMessage] = useState('')

  async function request(url, options) {
    const response = await fetch(url, options)

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.message || '요청에 실패했습니다.')
    }

    if (response.status === 204) {
      return null
    }

    return response.json()
  }

  async function loadTasks() {
    try {
      setMessage('')

      const params = new URLSearchParams({
        requesterId,
        page: '0',
        size: '20'
      })

      if (keyword) {
        params.append('keyword', keyword)
      }

      if (status) {
        params.append('status', status)
      }

      const data = await request(
        `/api/projects/${projectId}/tasks?${params}`
      )

      setTasks(data.content)
    } catch (error) {
      setTasks([])
      setMessage(error.message)
    }
  }

  useEffect(() => {
    loadTasks()
  }, [])

  async function createUser(event) {
    event.preventDefault()

    try {
      const user = await request('/api/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          name: userName,
          email: userEmail
        })
      })

      setUserName('')
      setUserEmail('')
      setMessage(`사용자를 등록했습니다. 사용자 ID는 ${user.id}입니다.`)
    } catch (error) {
      setMessage(error.message)
    }
  }

  async function createTask(event) {
    event.preventDefault()

    try {
      await request(
        `/api/projects/${projectId}/tasks?requesterId=${requesterId}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            title,
            description,
            assigneeId: assigneeId ? Number(assigneeId) : null
          })
        }
      )

      setTitle('')
      setDescription('')
      setMessage('작업을 생성했습니다.')
      await loadTasks()
    } catch (error) {
      setMessage(error.message)
    }
  }

  async function changeStatus(task, nextStatus) {
    try {
      await request(
        `/api/projects/${projectId}/tasks/${task.id}?requesterId=${requesterId}`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            title: task.title,
            description: task.description,
            assigneeId: task.assigneeId,
            status: nextStatus,
            version: task.version
          })
        }
      )

      setMessage('상태를 변경했습니다.')
      await loadTasks()
    } catch (error) {
      setMessage(error.message)
    }
  }

  async function deleteTask(taskId) {
    try {
      await request(
        `/api/projects/${projectId}/tasks/${taskId}?requesterId=${requesterId}`,
        {
          method: 'DELETE'
        }
      )

      setMessage('작업을 삭제했습니다.')
      await loadTasks()
    } catch (error) {
      setMessage(error.message)
    }
  }

  return (
    <main>
      <h1>프로젝트 작업 관리</h1>

      <section>
        <h2>조회 조건</h2>

        <label>
          프로젝트 ID
          <input
            value={projectId}
            onChange={(event) => setProjectId(event.target.value)}
          />
        </label>

        <label>
          요청자 ID
          <input
            value={requesterId}
            onChange={(event) => setRequesterId(event.target.value)}
          />
        </label>

        <label>
          검색어
          <input
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
        </label>

        <label>
          상태
          <select
            value={status}
            onChange={(event) => setStatus(event.target.value)}
          >
            <option value="">전체</option>
            <option value="TODO">TODO</option>
            <option value="IN_PROGRESS">IN_PROGRESS</option>
            <option value="REVIEW">REVIEW</option>
            <option value="DONE">DONE</option>
          </select>
        </label>

        <button onClick={loadTasks}>조회</button>
      </section>

      <section>
        <h2>사용자 등록</h2>

        <form onSubmit={createUser}>
          <label>
            이름
            <input
              value={userName}
              onChange={(event) => setUserName(event.target.value)}
              required
            />
          </label>

          <label>
            이메일
            <input
              type="email"
              value={userEmail}
              onChange={(event) => setUserEmail(event.target.value)}
              required
            />
          </label>

          <button type="submit">사용자 등록</button>
        </form>
      </section>

      <section>
        <h2>작업 생성</h2>

        <form onSubmit={createTask}>
          <div>
            <label>
              제목
              <input
                value={title}
                onChange={(event) => setTitle(event.target.value)}
                required
              />
            </label>
          </div>

          <div>
            <label>
              설명
              <input
                value={description}
                onChange={(event) => setDescription(event.target.value)}
              />
            </label>
          </div>

          <div>
            <label>
              담당자 ID
              <input
                type="number"
                value={assigneeId}
                onChange={(event) => setAssigneeId(event.target.value)}
              />
            </label>
          </div>

          <button type="submit">생성</button>
        </form>
      </section>

      <section>
        <h2>작업 목록</h2>

        {message && <p>{message}</p>}

        <table border="1">
          <thead>
            <tr>
              <th>ID</th>
              <th>제목</th>
              <th>설명</th>
              <th>상태</th>
              <th>담당자</th>
              <th>기능</th>
            </tr>
          </thead>
          <tbody>
            {tasks.map((task) => (
              <tr key={task.id}>
                <td>{task.id}</td>
                <td>{task.title}</td>
                <td>{task.description}</td>
                <td>{task.status}</td>
                <td>{task.assigneeName || '없음'}</td>
                <td>
                  <button onClick={() => changeStatus(task, 'IN_PROGRESS')}>
                    진행중
                  </button>
                  <button onClick={() => changeStatus(task, 'DONE')}>
                    완료
                  </button>
                  <button onClick={() => deleteTask(task.id)}>삭제</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </main>
  )
}

export default App
