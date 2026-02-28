import React, { useEffect, useState } from "react";
import { request } from "../lib/api";

const emptyPost = { issueId: "", title: "", content: "" };
const emptyComment = { issueId: "", content: "" };
const emptyLocation = { lat: "", lng: "", km: "2" };

export default function CommunityPage() {
  const [posts, setPosts] = useState([]);
  const [nearby, setNearby] = useState([]);
  const [upvoted, setUpvoted] = useState([]);
  const [postForm, setPostForm] = useState(emptyPost);
  const [commentForm, setCommentForm] = useState(emptyComment);
  const [location, setLocation] = useState(emptyLocation);
  const [inlineComment, setInlineComment] = useState({});
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadPosts = async () => {
    const data = await request("/api/v2/community/posts", { auth: true });
    setPosts(Array.isArray(data) ? data : []);
  };

  const loadUpvoted = async () => {
    const data = await request("/api/v2/community/upvoted", { auth: true });
    setUpvoted(Array.isArray(data) ? data : []);
  };

  useEffect(() => {
    loadPosts();
    loadUpvoted();
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const lat = pos.coords.latitude.toFixed(6);
          const lng = pos.coords.longitude.toFixed(6);
          setLocation((s) => ({ ...s, lat, lng }));
        },
        () => {
          setError("Location permission denied. Enter location manually.");
        }
      );
    } else {
      setError("Geolocation not supported by this browser.");
    }
  }, []);

  const loadNearby = async () => {
    setError("");
    if (!location.lat || !location.lng) {
      setError("Allow location access to load nearby issues.");
      return;
    }
    const data = await request(
      `/api/v2/community/issues?lat=${location.lat}&lng=${location.lng}&km=${location.km}`,
      { auth: true }
    );
    setNearby(Array.isArray(data) ? data : []);
  };

  const createPost = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    try {
      await request("/api/v2/community/posts", {
        method: "POST",
        auth: true,
        body: JSON.stringify(postForm),
      });
      setPostForm(emptyPost);
      setMessage("Community post created.");
      loadPosts();
    } catch (err) {
      setError(err.message);
    }
  };

  const upvote = async (issueId) => {
    await request(`/api/v2/issues/${issueId}/upvote`, {
      method: "POST",
      auth: true,
    });
    loadPosts();
    loadUpvoted();
    if (nearby.length) {
      loadNearby();
    }
  };

  const comment = async (e) => {
    e.preventDefault();
    await request(`/api/v2/issues/${commentForm.issueId}/comment`, {
      method: "POST",
      auth: true,
      body: JSON.stringify({ content: commentForm.content }),
    });
    setCommentForm(emptyComment);
    loadPosts();
    loadUpvoted();
  };

  const commentInline = async (issueId) => {
    const content = inlineComment[issueId];
    if (!content) return;
    await request(`/api/v2/issues/${issueId}/comment`, {
      method: "POST",
      auth: true,
      body: JSON.stringify({ content }),
    });
    setInlineComment((s) => ({ ...s, [issueId]: "" }));
    loadPosts();
    loadUpvoted();
  };

  return (
    <div className="stack">
      <section className="hero">
        <div>
          <h2>Community Feed</h2>
          <p className="note">See nearby issues and community updates.</p>
        </div>
      </section>

      <div className="grid two-cols">
        <form className="form-card" onSubmit={createPost}>
          <h3>Create Post</h3>
          <input
            placeholder="Issue ID"
            value={postForm.issueId}
            onChange={(e) =>
              setPostForm((s) => ({ ...s, issueId: e.target.value }))
            }
            required
          />
          <input
            placeholder="Post title"
            value={postForm.title}
            onChange={(e) =>
              setPostForm((s) => ({ ...s, title: e.target.value }))
            }
            required
          />
          <textarea
            placeholder="Post content"
            value={postForm.content}
            onChange={(e) =>
              setPostForm((s) => ({ ...s, content: e.target.value }))
            }
          />
          <button type="submit">Publish</button>
          {message ? <p className="success">{message}</p> : null}
          {error ? <p className="error">{error}</p> : null}
        </form>

        <form className="form-card" onSubmit={comment}>
          <h3>Add Comment by Issue ID</h3>
          <input
            placeholder="Issue ID"
            value={commentForm.issueId}
            onChange={(e) =>
              setCommentForm((s) => ({ ...s, issueId: e.target.value }))
            }
            required
          />
          <textarea
            placeholder="Comment"
            value={commentForm.content}
            onChange={(e) =>
              setCommentForm((s) => ({ ...s, content: e.target.value }))
            }
            required
          />
          <button type="submit">Comment</button>
        </form>
      </div>

      <div className="grid two-cols">
        <div className="form-card">
          <h3>Nearby Issues</h3>
          <div className="grid two-cols">
            <input
              placeholder="Latitude"
              value={location.lat}
              onChange={(e) =>
                setLocation((s) => ({ ...s, lat: e.target.value }))
              }
            />
            <input
              placeholder="Longitude"
              value={location.lng}
              onChange={(e) =>
                setLocation((s) => ({ ...s, lng: e.target.value }))
              }
            />
          </div>
          <input
            placeholder="Distance (km)"
            value={location.km}
            onChange={(e) =>
              setLocation((s) => ({ ...s, km: e.target.value }))
            }
          />
          <div className="row">
            <button
              type="button"
              className="ghost"
              onClick={() =>
                navigator.geolocation?.getCurrentPosition(
                  (pos) => {
                    const lat = pos.coords.latitude.toFixed(6);
                    const lng = pos.coords.longitude.toFixed(6);
                    setLocation((s) => ({ ...s, lat, lng }));
                  },
                  () => {
                    setError("Location permission denied. Enter location manually.");
                  }
                )
              }
            >
              Use my location
            </button>
            <button type="button" onClick={loadNearby}>
              Load nearby issues
            </button>
          </div>
          {nearby.length === 0 ? (
            <p className="note">No nearby issues loaded.</p>
          ) : (
            <ul className="list">
              {nearby.map((issue) => (
                <li key={issue.id}>
                  <strong>{issue.title}</strong>
                  <span className="muted">
                    {issue.category} - Severity{" "}
                    {issue.severity?.toFixed?.(1) || issue.severity}
                  </span>
                  <div className="row">
                    <button
                      type="button"
                      className="ghost"
                      onClick={() => upvote(issue.id)}
                    >
                      Upvote ({issue.upvotes})
                    </button>
                    <span className="chip">{issue.comments} comments</span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="result-card">
          <h3>Upvoted Issues</h3>
          {upvoted.length === 0 ? (
            <p className="note">No upvoted issues yet.</p>
          ) : (
            <ul className="list">
              {upvoted.map((issue) => (
                <li key={issue.id}>
                  <strong>{issue.title}</strong>
                  <span className="muted">
                    {issue.category} - Severity{" "}
                    {issue.severity?.toFixed?.(1) || issue.severity}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <div className="feed">
        {posts.length === 0 ? (
          <div className="result-card">
            <h3>Community Feed</h3>
            <p className="note">No posts yet.</p>
          </div>
        ) : (
          posts.map((post) => (
            <article key={post.postId} className="feed-card">
              {post.issue?.photoUrl ? (
                <img src={post.issue.photoUrl} alt={post.title} />
              ) : null}
              <div className="feed-body">
                <div className="feed-header">
                  <h4>{post.title}</h4>
                  <span className="muted">
                    Issue #{post.issue?.id} - {post.issue?.category}
                  </span>
                </div>
                <p>{post.content}</p>
                <div className="row">
                  <button
                    type="button"
                    className="ghost"
                    onClick={() => upvote(post.issue?.id)}
                  >
                    Upvote ({post.issue?.upvotes})
                  </button>
                  <span className="chip">{post.issue?.comments} comments</span>
                </div>
                <div className="comment-row">
                  <input
                    placeholder="Add a comment"
                    value={inlineComment[post.issue?.id] || ""}
                    onChange={(e) =>
                      setInlineComment((s) => ({
                        ...s,
                        [post.issue?.id]: e.target.value,
                      }))
                    }
                  />
                  <button
                    type="button"
                    onClick={() => commentInline(post.issue?.id)}
                  >
                    Send
                  </button>
                </div>
              </div>
            </article>
          ))
        )}
      </div>
    </div>
  );
}
