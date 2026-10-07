import React, { type ButtonHTMLAttributes } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  loading?: boolean;
  variant?: string;
  size?: string;
}

export const Button: React.FC<ButtonProps> = ({ loading, variant, size, children, ...props }) => {
  return (
    <button className={`px-4 py-2 rounded-md ${variant === 'outline' ? 'border' : 'bg-teal-600 text-white'}`} disabled={loading || props.disabled} {...props}>
      {loading ? 'Loading...' : children}
    </button>
  );
};
