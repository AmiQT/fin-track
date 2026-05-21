import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import MyLeaves from './MyLeaves';

const mockShowToast = vi.fn();

vi.mock('../context/ToastContext', () => ({
  useToast: () => ({ showToast: mockShowToast }),
}));

const mockLeaves = [
  {
    id: 1,
    leaveType: 'ANNUAL',
    startDate: '2025-01-06',
    endDate: '2025-01-08',
    totalDays: 3,
    reason: 'Family trip',
    status: 'APPROVED',
    appliedAt: '2025-01-02T10:00:00',
  },
  {
    id: 2,
    leaveType: 'SICK',
    startDate: '2025-02-10',
    endDate: '2025-02-10',
    totalDays: 1,
    reason: '',
    status: 'PENDING',
    appliedAt: '2025-02-10T08:00:00',
  },
];

const mockBalance = {
  annualEntitlement: 14,
  annualTaken: 3,
  annualBalance: 11,
  sickEntitlement: 14,
  sickTaken: 1,
  sickBalance: 13,
};

vi.mock('../services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

import api from '../services/api';

describe('MyLeaves', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading spinner on initial render', () => {
    api.get.mockReturnValue(new Promise(() => {}));
    render(<MyLeaves />);
    expect(document.querySelector('svg.animate-spin')).toBeInTheDocument();
  });

  it('shows empty state when no leaves exist', async () => {
    api.get.mockImplementation((url) => {
      if (url === '/leaves/my') return Promise.resolve({ data: [] });
      if (url === '/leaves/balance') return Promise.resolve({ data: mockBalance });
    });

    render(<MyLeaves />);
    await waitFor(() => {
      expect(screen.getByText('No leave history')).toBeInTheDocument();
    });
  });

  it('renders leave records in table', async () => {
    api.get.mockImplementation((url) => {
      if (url === '/leaves/my') return Promise.resolve({ data: mockLeaves });
      if (url === '/leaves/balance') return Promise.resolve({ data: mockBalance });
    });

    render(<MyLeaves />);
    await waitFor(() => {
      expect(screen.getByText('ANNUAL')).toBeInTheDocument();
      expect(screen.getByText('SICK')).toBeInTheDocument();
      expect(screen.getByText('Family trip')).toBeInTheDocument();
    });
  });

  it('renders leave status badges', async () => {
    api.get.mockImplementation((url) => {
      if (url === '/leaves/my') return Promise.resolve({ data: mockLeaves });
      if (url === '/leaves/balance') return Promise.resolve({ data: mockBalance });
    });

    render(<MyLeaves />);
    await waitFor(() => {
      expect(screen.getByText('APPROVED')).toBeInTheDocument();
      expect(screen.getByText('PENDING')).toBeInTheDocument();
    });
  });

  it('shows balance cards with correct values', async () => {
    api.get.mockImplementation((url) => {
      if (url === '/leaves/my') return Promise.resolve({ data: mockLeaves });
      if (url === '/leaves/balance') return Promise.resolve({ data: mockBalance });
    });

    render(<MyLeaves />);
    await waitFor(() => {
      expect(screen.getByText('11')).toBeInTheDocument();
      expect(screen.getByText('13')).toBeInTheDocument();
    });
  });

  it('opens the apply leave modal when Request Leave is clicked', async () => {
    api.get.mockImplementation((url) => {
      if (url === '/leaves/my') return Promise.resolve({ data: [] });
      if (url === '/leaves/balance') return Promise.resolve({ data: mockBalance });
    });

    const user = userEvent.setup();
    render(<MyLeaves />);
    await waitFor(() => screen.getByText('No leave history'));

    await user.click(screen.getByRole('button', { name: /request leave/i }));
    expect(screen.getByText('Apply for Leave')).toBeInTheDocument();
  });

  it('closes modal when Cancel is clicked', async () => {
    api.get.mockImplementation((url) => {
      if (url === '/leaves/my') return Promise.resolve({ data: [] });
      if (url === '/leaves/balance') return Promise.resolve({ data: mockBalance });
    });

    const user = userEvent.setup();
    render(<MyLeaves />);
    await waitFor(() => screen.getByText('No leave history'));

    await user.click(screen.getByRole('button', { name: /request leave/i }));
    expect(screen.getByText('Apply for Leave')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /cancel/i }));
    await waitFor(() => {
      expect(screen.queryByText('Apply for Leave')).not.toBeInTheDocument();
    });
  });

  it('submits leave form and calls POST /leaves', async () => {
    api.get.mockImplementation((url) => {
      if (url === '/leaves/my') return Promise.resolve({ data: [] });
      if (url === '/leaves/balance') return Promise.resolve({ data: mockBalance });
    });
    api.post.mockResolvedValue({ data: {} });

    const user = userEvent.setup();
    const { container } = render(<MyLeaves />);
    await waitFor(() => screen.getByText('No leave history'));

    await user.click(screen.getByRole('button', { name: /request leave/i }));

    const [startInput, endInput] = container.querySelectorAll('input[type="date"]');
    await user.type(startInput, '2025-03-10');
    await user.type(endInput, '2025-03-11');
    await user.click(screen.getByRole('button', { name: /submit request/i }));

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/leaves', expect.objectContaining({
        leaveType: 'ANNUAL',
        startDate: '2025-03-10',
        endDate: '2025-03-11',
      }));
    });
  });

  it('shows error toast when API call fails', async () => {
    api.get.mockRejectedValue(new Error('Network Error'));

    render(<MyLeaves />);
    await waitFor(() => {
      expect(mockShowToast).toHaveBeenCalledWith('Failed to load leave history.', 'error');
    });
  });
});
