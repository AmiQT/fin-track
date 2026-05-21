import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Login from './Login';

const mockNavigate = vi.fn();
const mockLogin = vi.fn();
const mockShowToast = vi.fn();

vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

vi.mock('../context/AuthContext', () => ({
  useAuth: () => ({ login: mockLogin }),
}));

vi.mock('../context/ToastContext', () => ({
  useToast: () => ({ showToast: mockShowToast }),
}));

describe('Login', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders email and password fields and submit button', () => {
    render(<Login />);
    expect(screen.getByPlaceholderText('admin@fintrack.com')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('••••••••')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('calls login with correct email and password on submit', async () => {
    mockLogin.mockResolvedValue({ success: true });
    const user = userEvent.setup();
    render(<Login />);

    await user.type(screen.getByPlaceholderText('admin@fintrack.com'), 'admin@fintrack.com');
    await user.type(screen.getByPlaceholderText('••••••••'), 'password123');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('admin@fintrack.com', 'password123');
    });
  });

  it('navigates to home on successful login', async () => {
    mockLogin.mockResolvedValue({ success: true });
    const user = userEvent.setup();
    render(<Login />);

    await user.type(screen.getByPlaceholderText('admin@fintrack.com'), 'admin@fintrack.com');
    await user.type(screen.getByPlaceholderText('••••••••'), 'password123');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  it('shows error message on failed login', async () => {
    mockLogin.mockResolvedValue({ success: false, message: 'Invalid credentials' });
    const user = userEvent.setup();
    render(<Login />);

    await user.type(screen.getByPlaceholderText('admin@fintrack.com'), 'wrong@email.com');
    await user.type(screen.getByPlaceholderText('••••••••'), 'wrongpass');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
    });
  });

  it('shows toast on successful login', async () => {
    mockLogin.mockResolvedValue({ success: true });
    const user = userEvent.setup();
    render(<Login />);

    await user.type(screen.getByPlaceholderText('admin@fintrack.com'), 'admin@fintrack.com');
    await user.type(screen.getByPlaceholderText('••••••••'), 'password123');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(mockShowToast).toHaveBeenCalledWith('Welcome back! Logging you in...', 'success');
    });
  });
});
