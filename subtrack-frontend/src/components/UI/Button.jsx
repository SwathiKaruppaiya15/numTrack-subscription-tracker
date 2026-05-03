export default function Button({ children, variant = "primary", className = "", ...props }) {
  return (
    <button className={`${variant === "ghost" ? "btn-ghost" : "btn-primary"} ${className}`} {...props}>
      {children}
    </button>
  );
}
